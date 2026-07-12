package com.dww.chat_app.service;

import com.dww.chat_app.dto.task.ChecklistItemCreationRequest;
import com.dww.chat_app.dto.task.ChecklistItemResponse;
import com.dww.chat_app.dto.task.ChecklistItemStatusUpdateRequest;
import com.dww.chat_app.dto.task.ChecklistItemUpdateRequest;
import com.dww.chat_app.entity.ChecklistItem;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.ChecklistItemMapper;
import com.dww.chat_app.repository.ChecklistItemRepository;
import com.dww.chat_app.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class ChecklistItemService {

    TaskRepository taskRepository;
    ChecklistItemRepository checklistItemRepository;
    ChecklistItemMapper checklistItemMapper;
    WorkspaceAccessService workspaceAccessService;

    public List<ChecklistItemResponse> getChecklistItems(UUID taskId) {
        Task task = getReadableTask(taskId);

        return checklistItemRepository.findAllByTaskIdOrderBySortOrderAsc(task.getId())
                .stream()
                .map(checklistItemMapper::toResponse)
                .toList();
    }

    public ChecklistItemResponse getChecklistItem(UUID taskId, UUID checklistItemId) {
        Task task = getReadableTask(taskId);

        return checklistItemMapper.toResponse(findChecklistItem(task.getId(), checklistItemId));
    }

    @Transactional
    public ChecklistItemResponse createChecklistItem(UUID taskId, ChecklistItemCreationRequest request) {
        Task task = getContributorTask(taskId);
        ChecklistItem checklistItem = checklistItemMapper.toEntity(request, task);

        return checklistItemMapper.toResponse(checklistItemRepository.save(checklistItem));
    }

    @Transactional
    public ChecklistItemResponse updateChecklistItem(
            UUID taskId,
            UUID checklistItemId,
            ChecklistItemUpdateRequest request
    ) {
        Task task = getContributorTask(taskId);
        ChecklistItem checklistItem = findChecklistItem(task.getId(), checklistItemId);

        checklistItemMapper.updateEntity(request, checklistItem);

        return checklistItemMapper.toResponse(checklistItemRepository.save(checklistItem));
    }

    @Transactional
    public ChecklistItemResponse updateChecklistItemStatus(
            UUID taskId,
            UUID checklistItemId,
            ChecklistItemStatusUpdateRequest request
    ) {
        Task task = getContributorTask(taskId);
        ChecklistItem checklistItem = findChecklistItem(task.getId(), checklistItemId);

        checklistItemMapper.updateStatus(request, checklistItem);

        return checklistItemMapper.toResponse(checklistItemRepository.save(checklistItem));
    }

    @Transactional
    public void deleteChecklistItem(UUID taskId, UUID checklistItemId) {
        Task task = getContributorTask(taskId);
        ChecklistItem checklistItem = findChecklistItem(task.getId(), checklistItemId);

        checklistItemRepository.delete(checklistItem);
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

    private ChecklistItem findChecklistItem(UUID taskId, UUID checklistItemId) {
        return checklistItemRepository.findByIdAndTaskId(checklistItemId, taskId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }
}
