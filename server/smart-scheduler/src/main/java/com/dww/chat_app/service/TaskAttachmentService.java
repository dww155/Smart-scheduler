package com.dww.chat_app.service;

import com.dww.chat_app.dto.task.TaskAttachmentCreationRequest;
import com.dww.chat_app.dto.task.TaskAttachmentDownload;
import com.dww.chat_app.dto.task.TaskAttachmentResponse;
import com.dww.chat_app.dto.task.TaskAttachmentUpdateRequest;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskAttachment;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.TaskAttachmentMapper;
import com.dww.chat_app.repository.TaskAttachmentRepository;
import com.dww.chat_app.repository.TaskRepository;
import com.dww.chat_app.service.storage.FileStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class TaskAttachmentService {

    TaskRepository taskRepository;
    TaskAttachmentRepository taskAttachmentRepository;
    TaskAttachmentMapper taskAttachmentMapper;
    WorkspaceAccessService workspaceAccessService;
    FileStorageService fileStorageService;

    public List<TaskAttachmentResponse> getAttachments(UUID taskId) {
        Task task = getReadableTask(taskId);

        return taskAttachmentRepository.findAllByTaskIdAndDeletedAtIsNullOrderByCreatedAtDesc(task.getId())
                .stream()
                .map(taskAttachmentMapper::toResponse)
                .toList();
    }

    public TaskAttachmentResponse getAttachment(UUID taskId, UUID attachmentId) {
        Task task = getReadableTask(taskId);

        return taskAttachmentMapper.toResponse(findActiveAttachment(task.getId(), attachmentId));
    }

    public TaskAttachmentDownload downloadAttachment(UUID taskId, UUID attachmentId) {
        Task task = getReadableTask(taskId);
        TaskAttachment attachment = findActiveAttachment(task.getId(), attachmentId);
        Resource resource;
        try {
            resource = fileStorageService.loadAsResource(attachment.getStorageKey());
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        return TaskAttachmentDownload.builder()
                .resource(resource)
                .originalFileName(attachment.getOriginalFileName())
                .contentType(attachment.getContentType())
                .sizeBytes(attachment.getSizeBytes())
                .build();
    }

    @Transactional
    public TaskAttachmentResponse createAttachment(UUID taskId, TaskAttachmentCreationRequest request) {
        Task task = getContributorTask(taskId);
        User currentUser = workspaceAccessService.getCurrentUser();
        String storageKey = fileStorageService.store(request.getFile());

        try {
            scheduleDeleteOnRollback(storageKey);

            TaskAttachment attachment = taskAttachmentMapper.toEntity(request, task, currentUser, storageKey);
            return taskAttachmentMapper.toResponse(taskAttachmentRepository.save(attachment));
        } catch (RuntimeException exception) {
            deleteFileQuietly(storageKey);
            throw exception;
        }
    }

    @Transactional
    public TaskAttachmentResponse updateAttachment(
            UUID taskId,
            UUID attachmentId,
            TaskAttachmentUpdateRequest request
    ) {
        Task task = getContributorTask(taskId);
        TaskAttachment attachment = findActiveAttachment(task.getId(), attachmentId);
        requireUploaderOrManager(task, attachment.getUploadedBy().getId());

        String previousStorageKey = attachment.getStorageKey();
        String replacementStorageKey = fileStorageService.store(request.getFile());

        try {
            scheduleDeleteOnRollback(replacementStorageKey);
            taskAttachmentMapper.updateEntity(request, replacementStorageKey, attachment);

            TaskAttachmentResponse response = taskAttachmentMapper.toResponse(taskAttachmentRepository.save(attachment));
            scheduleDeleteAfterCommit(previousStorageKey);

            return response;
        } catch (RuntimeException exception) {
            deleteFileQuietly(replacementStorageKey);
            throw exception;
        }
    }

    @Transactional
    public void deleteAttachment(UUID taskId, UUID attachmentId) {
        Task task = getContributorTask(taskId);
        TaskAttachment attachment = findActiveAttachment(task.getId(), attachmentId);

        requireUploaderOrManager(task, attachment.getUploadedBy().getId());
        attachment.setDeletedAt(LocalDateTime.now());
        taskAttachmentRepository.save(attachment);

        scheduleDeleteAfterCommit(attachment.getStorageKey());
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

    private TaskAttachment findActiveAttachment(UUID taskId, UUID attachmentId) {
        return taskAttachmentRepository.findByIdAndTaskIdAndDeletedAtIsNull(attachmentId, taskId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private void requireUploaderOrManager(Task task, UUID uploaderId) {
        if (!workspaceAccessService.isCurrentUser(uploaderId)) {
            workspaceAccessService.requireManager(task.getProject().getWorkspace());
        }
    }

    private void scheduleDeleteOnRollback(String storageKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    deleteFileQuietly(storageKey);
                }
            }
        });
    }

    private void scheduleDeleteAfterCommit(String storageKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteFileQuietly(storageKey);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteFileQuietly(storageKey);
            }
        });
    }

    private void deleteFileQuietly(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }

        try {
            fileStorageService.delete(storageKey);
        } catch (RuntimeException exception) {
            log.warn("Unable to delete stored attachment file", exception);
        }
    }
}
