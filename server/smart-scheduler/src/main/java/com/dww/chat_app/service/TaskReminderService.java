package com.dww.chat_app.service;

import com.dww.chat_app.dto.task.TaskReminderCreationRequest;
import com.dww.chat_app.dto.task.TaskReminderResponse;
import com.dww.chat_app.dto.task.TaskReminderUpdateRequest;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskReminder;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.enums.ReminderStatus;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.TaskReminderMapper;
import com.dww.chat_app.repository.TaskReminderRepository;
import com.dww.chat_app.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class TaskReminderService {

    TaskRepository taskRepository;
    TaskReminderRepository taskReminderRepository;
    TaskReminderMapper taskReminderMapper;
    TaskValidationService taskValidationService;
    WorkspaceAccessService workspaceAccessService;

    public List<TaskReminderResponse> getReminders(UUID taskId) {
        Task task = getReadableTask(taskId);
        User currentUser = workspaceAccessService.getCurrentUser();

        return taskReminderRepository.findAllByTaskIdAndRecipientIdOrderByRemindAtAsc(task.getId(), currentUser.getId())
                .stream()
                .map(taskReminderMapper::toResponse)
                .toList();
    }

    public TaskReminderResponse getReminder(UUID taskId, UUID reminderId) {
        Task task = getReadableTask(taskId);
        TaskReminder reminder = findReminder(task.getId(), reminderId);

        requireRecipientOrManager(task, reminder.getRecipient().getId());

        return taskReminderMapper.toResponse(reminder);
    }

    @Transactional
    public TaskReminderResponse createReminder(UUID taskId, TaskReminderCreationRequest request) {
        Task task = getContributorTask(taskId);
        User currentUser = workspaceAccessService.getCurrentUser();

        validateReminder(request.getRemindAt(), request.getTimeZone());

        TaskReminder reminder = taskReminderMapper.toEntity(request, task, currentUser);
        reminder.setStatus(ReminderStatus.PENDING);

        return taskReminderMapper.toResponse(taskReminderRepository.save(reminder));
    }

    @Transactional
    public TaskReminderResponse updateReminder(
            UUID taskId,
            UUID reminderId,
            TaskReminderUpdateRequest request
    ) {
        Task task = getContributorTask(taskId);
        TaskReminder reminder = findReminder(task.getId(), reminderId);

        requireRecipientOrManager(task, reminder.getRecipient().getId());
        validateReminder(request.getRemindAt(), request.getTimeZone());

        taskReminderMapper.updateEntity(request, reminder);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setSentAt(null);

        return taskReminderMapper.toResponse(taskReminderRepository.save(reminder));
    }

    @Transactional
    public void deleteReminder(UUID taskId, UUID reminderId) {
        Task task = getContributorTask(taskId);
        TaskReminder reminder = findReminder(task.getId(), reminderId);

        requireRecipientOrManager(task, reminder.getRecipient().getId());
        reminder.setStatus(ReminderStatus.CANCELLED);
        taskReminderRepository.save(reminder);
    }

    private Task getReadableTask(UUID taskId) {
        Task task = getActiveTask(taskId);
        workspaceAccessService.requireReadable(task.getProject().getWorkspace());

        return task;
    }

    private Task getContributorTask(UUID taskId) {
        Task task = getActiveTask(taskId);
        workspaceAccessService.requireContributor(task.getProject().getWorkspace());

        return task;
    }

    private Task getActiveTask(UUID taskId) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (task.getArchivedAt() != null
                || task.getProject().getDeletedAt() != null
                || task.getProject().getArchivedAt() != null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        return task;
    }

    private TaskReminder findReminder(UUID taskId, UUID reminderId) {
        return taskReminderRepository.findByIdAndTaskId(reminderId, taskId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private void requireRecipientOrManager(Task task, UUID recipientId) {
        if (!workspaceAccessService.isCurrentUser(recipientId)) {
            workspaceAccessService.requireManager(task.getProject().getWorkspace());
        }
    }

    private void validateReminder(LocalDateTime remindAt, String timeZone) {
        if (remindAt == null || !remindAt.isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        taskValidationService.validateTimeZone(timeZone);
    }
}
