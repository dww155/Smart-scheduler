package com.dww.chat_app.service;

import com.dww.chat_app.dto.task.TaskCreationRequest;
import com.dww.chat_app.dto.task.TaskResponse;
import com.dww.chat_app.dto.task.TaskStatusUpdateRequest;
import com.dww.chat_app.dto.task.TaskUpdateRequest;
import com.dww.chat_app.entity.Label;
import com.dww.chat_app.entity.Project;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskSection;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.TaskMapper;
import com.dww.chat_app.repository.LabelRepository;
import com.dww.chat_app.repository.ProjectRepository;
import com.dww.chat_app.repository.TaskRepository;
import com.dww.chat_app.repository.TaskSectionRepository;
import com.dww.chat_app.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class TaskService {

    TaskRepository taskRepository;
    ProjectRepository projectRepository;
    TaskSectionRepository taskSectionRepository;
    LabelRepository labelRepository;
    UserRepository userRepository;
    TaskMapper taskMapper;
    WorkspaceAccessService workspaceAccessService;
    TaskValidationService taskValidationService;

    public List<TaskResponse> getTasksByProject(UUID projectId) {
        Project project = getActiveProject(projectId);
        workspaceAccessService.requireReadable(project.getWorkspace());

        return taskRepository.findAllByProjectIdAndDeletedAtIsNullOrderBySortOrderAsc(project.getId())
                .stream()
                .filter(task -> task.getArchivedAt() == null)
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional
    public TaskResponse createTask(UUID projectId, TaskCreationRequest request) {
        Project project = getActiveProject(projectId);
        workspaceAccessService.requireContributor(project.getWorkspace());

        taskValidationService.validateTaskSchedule(
                request.getStartAt(),
                request.getDueAt(),
                request.getRecurrenceMode(),
                request.getRecurrenceRule(),
                request.getTimeZone()
        );

        TaskSection section = resolveSection(request.getSectionId(), project);
        Task parentTask = resolveParentTask(request.getParentTaskId(), project, null);
        Set<Label> labels = resolveLabels(request.getLabelIds(), project);
        User assignee = resolveAssignee(request.getAssigneeId(), project);
        User currentUser = workspaceAccessService.getCurrentUser();

        Task task = taskMapper.toEntity(
                request,
                project,
                section,
                parentTask,
                currentUser,
                assignee,
                labels
        );

        return taskMapper.toResponse(taskRepository.save(task));
    }

    public TaskResponse getTask(UUID taskId) {
        TaskContext context = getActiveTaskContext(taskId);
        workspaceAccessService.requireReadable(context.project().getWorkspace());

        return taskMapper.toResponse(context.task());
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, TaskUpdateRequest request) {
        TaskContext context = getActiveTaskContext(taskId);
        workspaceAccessService.requireContributor(context.project().getWorkspace());

        taskValidationService.validateTaskSchedule(
                request.getStartAt(),
                request.getDueAt(),
                request.getRecurrenceMode(),
                request.getRecurrenceRule(),
                request.getTimeZone()
        );

        TaskSection section = resolveSection(request.getSectionId(), context.project());
        Task parentTask = resolveParentTask(request.getParentTaskId(), context.project(), context.task().getId());
        validateNoParentCycle(context.task(), parentTask);
        Set<Label> labels = resolveLabels(request.getLabelIds(), context.project());
        User assignee = resolveAssignee(request.getAssigneeId(), context.project());

        taskMapper.updateEntity(request, section, parentTask, assignee, labels, context.task());

        return taskMapper.toResponse(taskRepository.save(context.task()));
    }

    @Transactional
    public void deleteTask(UUID taskId) {
        TaskContext context = getActiveTaskContext(taskId);
        workspaceAccessService.requireContributor(context.project().getWorkspace());

        context.task().setDeletedAt(LocalDateTime.now());
        taskRepository.save(context.task());
    }

    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, TaskStatusUpdateRequest request) {
        TaskContext context = getActiveTaskContext(taskId);
        workspaceAccessService.requireContributor(context.project().getWorkspace());

        taskMapper.updateStatus(request, context.task());

        return taskMapper.toResponse(taskRepository.save(context.task()));
    }

    public List<TaskResponse> getSubtasks(UUID taskId) {
        TaskContext context = getActiveTaskContext(taskId);
        workspaceAccessService.requireReadable(context.project().getWorkspace());

        return taskRepository.findAllByParentTaskIdAndDeletedAtIsNullOrderBySortOrderAsc(context.task().getId())
                .stream()
                .filter(task -> task.getArchivedAt() == null)
                .map(taskMapper::toResponse)
                .toList();
    }

    private Project getActiveProject(UUID projectId) {
        if (projectId == null) {
            throw notFound();
        }

        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(this::notFound);
        if (project.getArchivedAt() != null) {
            throw notFound();
        }

        return project;
    }

    private TaskContext getActiveTaskContext(UUID taskId) {
        if (taskId == null) {
            throw notFound();
        }

        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(this::notFound);
        if (task.getArchivedAt() != null) {
            throw notFound();
        }
        Project project = getActiveProject(task.getProject().getId());

        return new TaskContext(task, project);
    }

    private TaskSection resolveSection(UUID sectionId, Project project) {
        if (sectionId == null) {
            return null;
        }

        TaskSection section = taskSectionRepository.findById(sectionId)
                .orElseThrow(this::invalidRequest);

        if (section.getArchivedAt() != null
                || !project.getId().equals(section.getProject().getId())) {
            throw invalidRequest();
        }

        return section;
    }

    private Task resolveParentTask(UUID parentTaskId, Project project, UUID taskId) {
        if (parentTaskId == null) {
            return null;
        }

        if (parentTaskId.equals(taskId)) {
            throw invalidRequest();
        }

        Task parentTask = taskRepository.findByIdAndDeletedAtIsNull(parentTaskId)
                .orElseThrow(this::invalidRequest);

        if (parentTask.getArchivedAt() != null
                || !project.getId().equals(parentTask.getProject().getId())) {
            throw invalidRequest();
        }

        return parentTask;
    }

    private Set<Label> resolveLabels(Set<UUID> labelIds, Project project) {
        if (labelIds == null || labelIds.stream().anyMatch(Objects::isNull)) {
            throw invalidRequest();
        }

        if (labelIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Label> labels = labelRepository
                .findAllByWorkspaceIdAndArchivedAtIsNullOrderByNameAsc(project.getWorkspace().getId())
                .stream()
                .filter(label -> labelIds.contains(label.getId()))
                .collect(Collectors.toCollection(HashSet::new));

        if (labels.size() != labelIds.size()) {
            throw invalidRequest();
        }

        return labels;
    }

    private User resolveAssignee(UUID assigneeId, Project project) {
        if (assigneeId == null) {
            return null;
        }

        User assignee = userRepository.findById(assigneeId)
                .filter(User::isActive)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(this::invalidRequest);

        if (!workspaceAccessService.isMember(project.getWorkspace().getId(), assignee.getId())) {
            throw invalidRequest();
        }

        return assignee;
    }

    private void validateNoParentCycle(Task task, Task parentTask) {
        if (parentTask == null) {
            return;
        }

        Set<UUID> visitedTaskIds = new HashSet<>();
        Task cursor = parentTask;

        while (cursor != null) {
            UUID cursorId = cursor.getId();
            if (cursorId == null
                    || cursorId.equals(task.getId())
                    || !visitedTaskIds.add(cursorId)) {
                throw invalidRequest();
            }

            Task parent = cursor.getParentTask();
            if (parent == null || parent.getId() == null) {
                return;
            }

            cursor = taskRepository.findByIdAndDeletedAtIsNull(parent.getId())
                    .orElseThrow(this::invalidRequest);

            if (cursor.getArchivedAt() != null
                    || !task.getProject().getId().equals(cursor.getProject().getId())) {
                throw invalidRequest();
            }
        }
    }

    private AppException notFound() {
        return new AppException(ErrorCode.NOT_FOUND);
    }

    private AppException invalidRequest() {
        return new AppException(ErrorCode.INVALID_REQUEST);
    }

    private record TaskContext(Task task, Project project) {
    }
}
