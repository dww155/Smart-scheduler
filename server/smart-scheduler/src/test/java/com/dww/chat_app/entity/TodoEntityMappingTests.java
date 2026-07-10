package com.dww.chat_app.entity;

import com.dww.chat_app.entity.enums.ProjectViewType;
import com.dww.chat_app.entity.enums.RecurrenceMode;
import com.dww.chat_app.entity.enums.ReminderChannel;
import com.dww.chat_app.entity.enums.ReminderStatus;
import com.dww.chat_app.entity.enums.TaskPriority;
import com.dww.chat_app.entity.enums.TaskStatus;
import com.dww.chat_app.entity.enums.WorkspaceMemberRole;
import com.dww.chat_app.entity.enums.WorkspaceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TodoEntityMappingTests {

    private final TestEntityManager entityManager;

    @Autowired
    TodoEntityMappingTests(TestEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Test
    void persistsRepresentativeTodoGraphWithExpectedDefaults() {
        String uniqueSuffix = UUID.randomUUID().toString();

        User user = entityManager.persist(User.builder()
                .username("todo-mapping-" + uniqueSuffix)
                .password("test-password")
                .build());

        Workspace workspace = entityManager.persist(Workspace.builder()
                .owner(user)
                .name("Personal workspace " + uniqueSuffix)
                .build());

        WorkspaceMember workspaceMember = entityManager.persist(WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceMemberRole.OWNER)
                .build());

        Project project = entityManager.persist(Project.builder()
                .workspace(workspace)
                .createdBy(user)
                .name("Todo project " + uniqueSuffix)
                .build());

        TaskSection section = entityManager.persist(TaskSection.builder()
                .project(project)
                .name("In progress")
                .build());

        Label label = entityManager.persist(Label.builder()
                .workspace(workspace)
                .createdBy(user)
                .name("mapping-" + uniqueSuffix)
                .color("#2563EB")
                .build());

        Task parentTask = Task.builder()
                .project(project)
                .section(section)
                .createdBy(user)
                .assignee(user)
                .title("Persist the representative todo graph")
                .build();
        parentTask.getLabels().add(label);
        entityManager.persist(parentTask);

        Task subtask = entityManager.persist(Task.builder()
                .project(project)
                .section(section)
                .parentTask(parentTask)
                .createdBy(user)
                .assignee(user)
                .title("Verify the parent task mapping")
                .build());

        ChecklistItem checklistItem = entityManager.persist(ChecklistItem.builder()
                .task(parentTask)
                .content("Flush the persistence context")
                .build());

        TaskReminder reminder = entityManager.persist(TaskReminder.builder()
                .task(parentTask)
                .recipient(user)
                .remindAt(LocalDateTime.now().plusHours(1))
                .timeZone("Asia/Ho_Chi_Minh")
                .build());

        TaskComment comment = entityManager.persist(TaskComment.builder()
                .task(parentTask)
                .author(user)
                .content("Representative mapping comment")
                .build());

        TaskAttachment attachment = entityManager.persist(TaskAttachment.builder()
                .task(parentTask)
                .uploadedBy(user)
                .originalFileName("mapping.txt")
                .storageKey("todo-entity-mapping/" + uniqueSuffix)
                .contentType("text/plain")
                .sizeBytes(128L)
                .build());

        entityManager.flush();

        assertThat(Arrays.asList(
                user.getId(),
                workspace.getId(),
                workspaceMember.getId(),
                project.getId(),
                section.getId(),
                label.getId(),
                parentTask.getId(),
                subtask.getId(),
                checklistItem.getId(),
                reminder.getId(),
                comment.getId(),
                attachment.getId()
        )).doesNotContainNull();

        UUID workspaceId = workspace.getId();
        UUID projectId = project.getId();
        UUID labelId = label.getId();
        UUID parentTaskId = parentTask.getId();
        UUID subtaskId = subtask.getId();
        UUID reminderId = reminder.getId();

        entityManager.clear();

        Workspace reloadedWorkspace = entityManager.find(Workspace.class, workspaceId);
        Project reloadedProject = entityManager.find(Project.class, projectId);
        Task reloadedParentTask = entityManager.find(Task.class, parentTaskId);
        Task reloadedSubtask = entityManager.find(Task.class, subtaskId);
        TaskReminder reloadedReminder = entityManager.find(TaskReminder.class, reminderId);

        assertThat(reloadedWorkspace.getType()).isEqualTo(WorkspaceType.PERSONAL);
        assertThat(reloadedProject.getViewType()).isEqualTo(ProjectViewType.LIST);
        assertThat(reloadedParentTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(reloadedParentTask.getPriority()).isEqualTo(TaskPriority.NONE);
        assertThat(reloadedParentTask.getRecurrenceMode()).isEqualTo(RecurrenceMode.NONE);
        assertThat(reloadedReminder.getChannel()).isEqualTo(ReminderChannel.IN_APP);
        assertThat(reloadedReminder.getStatus()).isEqualTo(ReminderStatus.PENDING);
        assertThat(workspaceMember.getRole()).isEqualTo(WorkspaceMemberRole.OWNER);
        assertThat(checklistItem.isCompleted()).isFalse();
        assertThat(parentTask.isAllDay()).isTrue();
        assertThat(attachment.getUpdatedAt()).isNotNull();

        assertThat(reloadedProject.getWorkspace().getId()).isEqualTo(workspaceId);
        assertThat(reloadedSubtask.getParentTask().getId()).isEqualTo(parentTaskId);
        assertThat(reloadedParentTask.getLabels())
                .extracting(Label::getId)
                .containsExactly(labelId);
    }
}
