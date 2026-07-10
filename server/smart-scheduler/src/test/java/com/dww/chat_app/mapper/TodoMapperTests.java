package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.task.ChecklistItemStatusUpdateRequest;
import com.dww.chat_app.dto.task.TaskAttachmentCreationRequest;
import com.dww.chat_app.dto.task.TaskCreationRequest;
import com.dww.chat_app.dto.task.TaskStatusUpdateRequest;
import com.dww.chat_app.dto.task.TaskUpdateRequest;
import com.dww.chat_app.dto.workspace.WorkspaceCreationRequest;
import com.dww.chat_app.dto.workspace.WorkspaceUpdateRequest;
import com.dww.chat_app.entity.ChecklistItem;
import com.dww.chat_app.entity.Label;
import com.dww.chat_app.entity.Project;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskAttachment;
import com.dww.chat_app.entity.TaskSection;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import com.dww.chat_app.entity.enums.RecurrenceMode;
import com.dww.chat_app.entity.enums.TaskPriority;
import com.dww.chat_app.entity.enums.TaskStatus;
import com.dww.chat_app.entity.enums.WorkspaceType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TodoMapperTests {

    WorkspaceMapper workspaceMapper = Mappers.getMapper(WorkspaceMapper.class);
    TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);
    ChecklistItemMapper checklistItemMapper = Mappers.getMapper(ChecklistItemMapper.class);
    TaskAttachmentMapper taskAttachmentMapper = Mappers.getMapper(TaskAttachmentMapper.class);

    @Test
    void workspaceMapperMapsCreationAndPreservesOwnerOnUpdate() {
        User owner = User.builder().username("owner").password("password").build();
        WorkspaceCreationRequest creationRequest = new WorkspaceCreationRequest();
        creationRequest.setName("Personal");
        creationRequest.setDescription("Initial description");

        Workspace workspace = workspaceMapper.toEntity(creationRequest, owner);

        assertThat(workspace.getOwner()).isSameAs(owner);
        assertThat(workspace.getName()).isEqualTo("Personal");
        assertThat(workspace.getType()).isEqualTo(WorkspaceType.PERSONAL);
        assertThat(workspace.getId()).isNull();

        WorkspaceUpdateRequest updateRequest = new WorkspaceUpdateRequest();
        updateRequest.setName("Team workspace");
        updateRequest.setType(WorkspaceType.TEAM);
        updateRequest.setDescription(null);
        updateRequest.setColor("#2563EB");

        workspaceMapper.updateEntity(updateRequest, workspace);

        assertThat(workspace.getOwner()).isSameAs(owner);
        assertThat(workspace.getName()).isEqualTo("Team workspace");
        assertThat(workspace.getDescription()).isNull();
        assertThat(workspace.getType()).isEqualTo(WorkspaceType.TEAM);
        assertThat(workspace.getColor()).isEqualTo("#2563EB");
    }

    @Test
    void taskMapperMapsResolvedRelationshipsAndSafeDefaults() {
        Project project = Project.builder().name("Project").build();
        TaskSection section = TaskSection.builder().name("Todo").project(project).build();
        Task parentTask = Task.builder().title("Parent").project(project).build();
        User creator = User.builder().username("creator").password("password").build();
        User assignee = User.builder().username("assignee").password("password").build();
        Label label = Label.builder().name("Important").build();
        Set<Label> labels = Set.of(label);

        TaskCreationRequest request = new TaskCreationRequest();
        request.setTitle("Create mapper tests");

        Task task = taskMapper.toEntity(
                request,
                project,
                section,
                parentTask,
                creator,
                assignee,
                labels
        );

        assertThat(task.getProject()).isSameAs(project);
        assertThat(task.getSection()).isSameAs(section);
        assertThat(task.getParentTask()).isSameAs(parentTask);
        assertThat(task.getCreatedBy()).isSameAs(creator);
        assertThat(task.getAssignee()).isSameAs(assignee);
        assertThat(task.getTitle()).isEqualTo("Create mapper tests");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task.getPriority()).isEqualTo(TaskPriority.NONE);
        assertThat(task.getRecurrenceMode()).isEqualTo(RecurrenceMode.NONE);
        assertThat(task.getLabels()).containsExactly(label);
    }

    @Test
    void taskMapperUsesFullUpdateSemanticsAndPreservesServerFields() {
        Project project = Project.builder().name("Project").build();
        User creator = User.builder().username("creator").password("password").build();
        TaskSection oldSection = TaskSection.builder().name("Old").project(project).build();
        Label oldLabel = Label.builder().name("Old label").build();
        Task task = Task.builder()
                .project(project)
                .section(oldSection)
                .createdBy(creator)
                .title("Old title")
                .description("Old description")
                .status(TaskStatus.IN_PROGRESS)
                .labels(new HashSet<>(Set.of(oldLabel)))
                .build();

        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTitle("Updated title");
        request.setPriority(TaskPriority.HIGH);
        request.setAllDay(true);
        request.setRecurrenceMode(RecurrenceMode.NONE);
        request.setSortOrder(2);
        request.setLabelIds(Set.of());

        taskMapper.updateEntity(request, null, null, null, Set.of(), task);

        assertThat(task.getProject()).isSameAs(project);
        assertThat(task.getCreatedBy()).isSameAs(creator);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getTitle()).isEqualTo("Updated title");
        assertThat(task.getDescription()).isNull();
        assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(task.getSection()).isNull();
        assertThat(task.getParentTask()).isNull();
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getLabels()).isEmpty();
    }

    @Test
    void statusMappersSynchronizeCompletionTimestamps() {
        Task task = Task.builder().title("Task").build();
        TaskStatusUpdateRequest taskStatusRequest = new TaskStatusUpdateRequest();
        taskStatusRequest.setStatus(TaskStatus.COMPLETED);

        taskMapper.updateStatus(taskStatusRequest, task);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();

        taskStatusRequest.setStatus(TaskStatus.TODO);
        taskMapper.updateStatus(taskStatusRequest, task);

        assertThat(task.getCompletedAt()).isNull();

        ChecklistItem checklistItem = ChecklistItem.builder().content("Step").build();
        ChecklistItemStatusUpdateRequest checklistStatusRequest =
                new ChecklistItemStatusUpdateRequest();
        checklistStatusRequest.setCompleted(true);

        checklistItemMapper.updateStatus(checklistStatusRequest, checklistItem);

        assertThat(checklistItem.isCompleted()).isTrue();
        assertThat(checklistItem.getCompletedAt()).isNotNull();

        checklistStatusRequest.setCompleted(false);
        checklistItemMapper.updateStatus(checklistStatusRequest, checklistItem);

        assertThat(checklistItem.getCompletedAt()).isNull();
    }

    @Test
    void attachmentMapperMapsMultipartMetadataAndServerStorageKey() {
        Task task = Task.builder().title("Task").build();
        User uploader = User.builder().username("uploader").password("password").build();
        TaskAttachmentCreationRequest request = new TaskAttachmentCreationRequest();
        request.setFile(new MockMultipartFile(
                "file",
                "../documents\\plan.txt",
                "text/plain",
                "content".getBytes(StandardCharsets.UTF_8)
        ));

        TaskAttachment attachment = taskAttachmentMapper.toEntity(
                request,
                task,
                uploader,
                "tasks/generated-storage-key"
        );

        assertThat(attachment.getTask()).isSameAs(task);
        assertThat(attachment.getUploadedBy()).isSameAs(uploader);
        assertThat(attachment.getOriginalFileName()).isEqualTo("plan.txt");
        assertThat(attachment.getStorageKey()).isEqualTo("tasks/generated-storage-key");
        assertThat(attachment.getContentType()).isEqualTo("text/plain");
        assertThat(attachment.getSizeBytes()).isEqualTo(7L);
    }
}
