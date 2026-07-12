package com.dww.chat_app.service;

import com.dww.chat_app.dto.project.ProjectCreationRequest;
import com.dww.chat_app.dto.project.ProjectResponse;
import com.dww.chat_app.dto.project.ProjectUpdateRequest;
import com.dww.chat_app.entity.Project;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.ProjectMapper;
import com.dww.chat_app.repository.ProjectRepository;
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
public class ProjectService {

    ProjectRepository projectRepository;
    ProjectMapper projectMapper;
    WorkspaceAccessService workspaceAccessService;

    @Transactional
    public ProjectResponse createProject(
            UUID workspaceId,
            ProjectCreationRequest request
    ) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);
        User currentUser = workspaceAccessService.getCurrentUser();

        Project project = projectMapper.toEntity(request, workspace, currentUser);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(UUID workspaceId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);

        return projectRepository.findAllByWorkspaceIdAndDeletedAtIsNullOrderBySortOrderAsc(workspaceId)
                .stream()
                .filter(project -> project.getArchivedAt() == null)
                .map(projectMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID workspaceId, UUID projectId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);

        return projectMapper.toResponse(findActiveProject(workspace, projectId));
    }

    @Transactional
    public ProjectResponse updateProject(
            UUID workspaceId,
            UUID projectId,
            ProjectUpdateRequest request
    ) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);

        Project project = findActiveProject(workspace, projectId);
        projectMapper.updateEntity(request, project);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID workspaceId, UUID projectId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);

        Project project = findActiveProject(workspace, projectId);
        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    private Project findActiveProject(Workspace workspace, UUID projectId) {
        if (projectId == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!workspace.getId().equals(project.getWorkspace().getId())
                || project.getArchivedAt() != null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        return project;
    }
}
