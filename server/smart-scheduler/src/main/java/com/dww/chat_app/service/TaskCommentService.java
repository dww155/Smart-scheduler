package com.dww.chat_app.service;

import com.dww.chat_app.dto.PageResponse;
import com.dww.chat_app.dto.task.TaskCommentCreationRequest;
import com.dww.chat_app.dto.task.TaskCommentResponse;
import com.dww.chat_app.dto.task.TaskCommentUpdateRequest;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskComment;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.TaskCommentMapper;
import com.dww.chat_app.repository.TaskCommentRepository;
import com.dww.chat_app.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class TaskCommentService {

    static final int MAX_PAGE_SIZE = 100;

    TaskRepository taskRepository;
    TaskCommentRepository taskCommentRepository;
    TaskCommentMapper taskCommentMapper;
    WorkspaceAccessService workspaceAccessService;

    public PageResponse<TaskCommentResponse> getComments(UUID taskId, int page, int size) {
        Task task = getReadableTask(taskId);
        validatePageRequest(page, size);

        Page<TaskCommentResponse> commentPage = taskCommentRepository
                .findAllByTaskIdAndDeletedAtIsNullOrderByCreatedAtDesc(task.getId(), PageRequest.of(page, size))
                .map(taskCommentMapper::toResponse);

        return PageResponse.from(commentPage);
    }

    public TaskCommentResponse getComment(UUID taskId, UUID commentId) {
        Task task = getReadableTask(taskId);

        return taskCommentMapper.toResponse(findActiveComment(task.getId(), commentId));
    }

    @Transactional
    public TaskCommentResponse createComment(UUID taskId, TaskCommentCreationRequest request) {
        Task task = getContributorTask(taskId);
        User currentUser = workspaceAccessService.getCurrentUser();
        TaskComment comment = taskCommentMapper.toEntity(request, task, currentUser);

        return taskCommentMapper.toResponse(taskCommentRepository.save(comment));
    }

    @Transactional
    public TaskCommentResponse updateComment(UUID taskId, UUID commentId, TaskCommentUpdateRequest request) {
        Task task = getContributorTask(taskId);
        TaskComment comment = findActiveComment(task.getId(), commentId);

        requireAuthorOrManager(task, comment.getAuthor().getId());
        taskCommentMapper.updateEntity(request, comment);

        return taskCommentMapper.toResponse(taskCommentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID taskId, UUID commentId) {
        Task task = getContributorTask(taskId);
        TaskComment comment = findActiveComment(task.getId(), commentId);

        requireAuthorOrManager(task, comment.getAuthor().getId());
        comment.setDeletedAt(LocalDateTime.now());
        taskCommentRepository.save(comment);
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

    private TaskComment findActiveComment(UUID taskId, UUID commentId) {
        return taskCommentRepository.findByIdAndTaskIdAndDeletedAtIsNull(commentId, taskId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private void requireAuthorOrManager(Task task, UUID authorId) {
        if (!workspaceAccessService.isCurrentUser(authorId)) {
            workspaceAccessService.requireManager(task.getProject().getWorkspace());
        }
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}
