package com.dww.chat_app.service;

import com.dww.chat_app.dto.project.TaskSectionCreationRequest;
import com.dww.chat_app.dto.project.TaskSectionResponse;
import com.dww.chat_app.dto.project.TaskSectionUpdateRequest;
import com.dww.chat_app.entity.Project;
import com.dww.chat_app.entity.TaskSection;
import com.dww.chat_app.entity.Workspace;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.TaskSectionMapper;
import com.dww.chat_app.repository.ProjectRepository;
import com.dww.chat_app.repository.TaskSectionRepository;
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
public class TaskSectionService {

    ProjectRepository projectRepository;
    TaskSectionRepository taskSectionRepository;
    TaskSectionMapper taskSectionMapper;
    WorkspaceAccessService workspaceAccessService;
    TaskRepository taskRepository;

    @Transactional
    public TaskSectionResponse createSection(
            UUID workspaceId,
            UUID projectId,
            TaskSectionCreationRequest request
    ) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);
        Project project = findActiveProject(workspace, projectId);

        TaskSection section = taskSectionMapper.toEntity(request, project);
        return taskSectionMapper.toResponse(taskSectionRepository.save(section));
    }

    @Transactional(readOnly = true)
    public List<TaskSectionResponse> getSections(UUID workspaceId, UUID projectId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);
        Project project = findActiveProject(workspace, projectId);

        return taskSectionRepository.findAllByProjectIdAndArchivedAtIsNullOrderBySortOrderAsc(project.getId())
                .stream()
                .map(taskSectionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskSectionResponse getSection(UUID workspaceId, UUID projectId, UUID sectionId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);
        Project project = findActiveProject(workspace, projectId);

        return taskSectionMapper.toResponse(findActiveSection(project, sectionId));
    }

    @Transactional
    public TaskSectionResponse updateSection(
            UUID workspaceId,
            UUID projectId,
            UUID sectionId,
            TaskSectionUpdateRequest request
    ) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);
        Project project = findActiveProject(workspace, projectId);
        TaskSection section = findActiveSection(project, sectionId);

        taskSectionMapper.updateEntity(request, section);
        return taskSectionMapper.toResponse(taskSectionRepository.save(section));
    }

    @Transactional
    public void archiveSection(UUID workspaceId, UUID projectId, UUID sectionId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);
        Project project = findActiveProject(workspace, projectId);
        TaskSection section = findActiveSection(project, sectionId);

        var affectedTasks = taskRepository.findAllBySectionIdAndDeletedAtIsNullOrderBySortOrderAsc(section.getId());
        affectedTasks.forEach(task -> task.setSection(null));
        taskRepository.saveAll(affectedTasks);

        section.setArchivedAt(LocalDateTime.now());
        taskSectionRepository.save(section);
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

    private TaskSection findActiveSection(Project project, UUID sectionId) {
        if (sectionId == null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        TaskSection section = taskSectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!project.getId().equals(section.getProject().getId())
                || section.getArchivedAt() != null) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        return section;
    }
}
