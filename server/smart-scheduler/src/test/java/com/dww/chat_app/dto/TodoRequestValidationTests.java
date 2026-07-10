package com.dww.chat_app.dto;

import com.dww.chat_app.dto.label.LabelCreationRequest;
import com.dww.chat_app.dto.project.ProjectCreationRequest;
import com.dww.chat_app.dto.task.ChecklistItemCreationRequest;
import com.dww.chat_app.dto.task.TaskAttachmentCreationRequest;
import com.dww.chat_app.dto.task.TaskCommentCreationRequest;
import com.dww.chat_app.dto.task.TaskCreationRequest;
import com.dww.chat_app.dto.task.TaskReminderCreationRequest;
import com.dww.chat_app.dto.task.TaskUpdateRequest;
import com.dww.chat_app.dto.workspace.WorkspaceCreationRequest;
import com.dww.chat_app.dto.workspace.WorkspaceMemberCreationRequest;
import com.dww.chat_app.entity.enums.ProjectViewType;
import com.dww.chat_app.entity.enums.RecurrenceMode;
import com.dww.chat_app.entity.enums.ReminderChannel;
import com.dww.chat_app.entity.enums.TaskPriority;
import com.dww.chat_app.entity.enums.WorkspaceMemberRole;
import com.dww.chat_app.entity.enums.WorkspaceType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TodoRequestValidationTests {

    static ValidatorFactory validatorFactory;
    static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void creationRequestsApplyExpectedDefaults() {
        WorkspaceCreationRequest workspaceRequest = new WorkspaceCreationRequest();
        workspaceRequest.setName("Personal workspace");

        WorkspaceMemberCreationRequest memberRequest = new WorkspaceMemberCreationRequest();
        memberRequest.setUserId(java.util.UUID.randomUUID());

        ProjectCreationRequest projectRequest = new ProjectCreationRequest();
        projectRequest.setName("Personal tasks");

        TaskCreationRequest taskRequest = new TaskCreationRequest();
        taskRequest.setTitle("Prepare weekly plan");

        TaskReminderCreationRequest reminderRequest = new TaskReminderCreationRequest();
        reminderRequest.setRemindAt(LocalDateTime.now().plusDays(1));

        assertThat(workspaceRequest.getType()).isEqualTo(WorkspaceType.PERSONAL);
        assertThat(memberRequest.getRole()).isEqualTo(WorkspaceMemberRole.MEMBER);
        assertThat(projectRequest.getViewType()).isEqualTo(ProjectViewType.LIST);
        assertThat(projectRequest.getSortOrder()).isZero();
        assertThat(taskRequest.getPriority()).isEqualTo(TaskPriority.NONE);
        assertThat(taskRequest.getRecurrenceMode()).isEqualTo(RecurrenceMode.NONE);
        assertThat(taskRequest.getAllDay()).isTrue();
        assertThat(taskRequest.getSortOrder()).isZero();
        assertThat(taskRequest.getLabelIds()).isEmpty();
        assertThat(reminderRequest.getChannel()).isEqualTo(ReminderChannel.IN_APP);

        assertThat(validator.validate(workspaceRequest)).isEmpty();
        assertThat(validator.validate(memberRequest)).isEmpty();
        assertThat(validator.validate(projectRequest)).isEmpty();
        assertThat(validator.validate(taskRequest)).isEmpty();
        assertThat(validator.validate(reminderRequest)).isEmpty();
    }

    @Test
    void blankCreationRequestsAreRejected() {
        assertThat(validator.validate(new WorkspaceCreationRequest())).isNotEmpty();
        assertThat(validator.validate(new WorkspaceMemberCreationRequest())).isNotEmpty();
        assertThat(validator.validate(new ProjectCreationRequest())).isNotEmpty();
        assertThat(validator.validate(new LabelCreationRequest())).isNotEmpty();
        assertThat(validator.validate(new TaskCreationRequest())).isNotEmpty();
        assertThat(validator.validate(new ChecklistItemCreationRequest())).isNotEmpty();
        assertThat(validator.validate(new TaskReminderCreationRequest())).isNotEmpty();
        assertThat(validator.validate(new TaskCommentCreationRequest())).isNotEmpty();
        assertThat(validator.validate(new TaskAttachmentCreationRequest())).isNotEmpty();
    }

    @Test
    void fullTaskUpdateRequiresCompleteCoreState() {
        Set<ConstraintViolation<TaskUpdateRequest>> violations = validator.validate(new TaskUpdateRequest());

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title", "priority", "allDay", "recurrenceMode", "sortOrder", "labelIds");
    }

    @Test
    void oversizedTaskTitleIsRejected() {
        TaskCreationRequest request = new TaskCreationRequest();
        request.setTitle("a".repeat(501));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title");
    }

    @Test
    void emptyAttachmentIsRejected() {
        TaskAttachmentCreationRequest request = new TaskAttachmentCreationRequest();
        request.setFile(new MockMultipartFile("file", new byte[0]));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("filePresent");
    }

    @Test
    void pastReminderIsRejected() {
        TaskReminderCreationRequest request = new TaskReminderCreationRequest();
        request.setRemindAt(LocalDateTime.now().minusMinutes(1));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("remindAt");
    }
}
