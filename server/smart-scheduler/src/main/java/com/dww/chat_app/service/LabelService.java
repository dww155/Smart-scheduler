package com.dww.chat_app.service;

import com.dww.chat_app.dto.label.LabelCreationRequest;
import com.dww.chat_app.dto.label.LabelResponse;
import com.dww.chat_app.dto.label.LabelUpdateRequest;
import com.dww.chat_app.entity.Label;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.LabelMapper;
import com.dww.chat_app.repository.LabelRepository;
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
public class LabelService {

    LabelRepository labelRepository;
    LabelMapper labelMapper;
    WorkspaceAccessService workspaceAccessService;
    TaskRepository taskRepository;

    @Transactional
    public LabelResponse createLabel(UUID workspaceId, LabelCreationRequest request) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);
        User currentUser = workspaceAccessService.getCurrentUser();

        Label label = labelMapper.toEntity(request, workspace, currentUser);
        return labelMapper.toResponse(labelRepository.save(label));
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> getLabels(UUID workspaceId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);

        return labelRepository.findAllByWorkspaceIdAndArchivedAtIsNullOrderByNameAsc(workspaceId)
                .stream()
                .map(labelMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LabelResponse getLabel(UUID workspaceId, UUID labelId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);

        return labelMapper.toResponse(findActiveLabel(workspace, labelId));
    }

    @Transactional
    public LabelResponse updateLabel(
            UUID workspaceId,
            UUID labelId,
            LabelUpdateRequest request
    ) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);
        Label label = findActiveLabel(workspace, labelId);

        labelMapper.updateEntity(request, label);
        return labelMapper.toResponse(labelRepository.save(label));
    }

    @Transactional
    public void archiveLabel(UUID workspaceId, UUID labelId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);
        Label label = findActiveLabel(workspace, labelId);

        var affectedTasks = taskRepository.findAllByLabelsIdAndDeletedAtIsNull(label.getId());
        affectedTasks.forEach(task -> task.getLabels().removeIf(item -> item.getId().equals(label.getId())));
        taskRepository.saveAll(affectedTasks);

        label.setArchivedAt(LocalDateTime.now());
        labelRepository.save(label);
    }

    private Label findActiveLabel(Workspace workspace, UUID labelId) {
        if (labelId == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        return labelRepository.findByIdAndWorkspaceId(labelId, workspace.getId())
                .filter(label -> label.getArchivedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }
}
