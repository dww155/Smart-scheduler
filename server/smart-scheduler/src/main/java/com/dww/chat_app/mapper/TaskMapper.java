package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.task.TaskCreationRequest;
import com.dww.chat_app.dto.task.TaskResponse;
import com.dww.chat_app.dto.task.TaskStatusUpdateRequest;
import com.dww.chat_app.dto.task.TaskUpdateRequest;
import com.dww.chat_app.entity.Label;
import com.dww.chat_app.entity.Project;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskSection;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.enums.TaskStatus;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(config = TodoMapperConfig.class)
public interface TaskMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "project", source = "project")
    @Mapping(target = "section", source = "section")
    @Mapping(target = "parentTask", source = "parentTask")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "priority", source = "request.priority")
    @Mapping(target = "startAt", source = "request.startAt")
    @Mapping(target = "dueAt", source = "request.dueAt")
    @Mapping(target = "allDay", source = "request.allDay")
    @Mapping(target = "recurrenceRule", source = "request.recurrenceRule")
    @Mapping(target = "recurrenceMode", source = "request.recurrenceMode")
    @Mapping(target = "timeZone", source = "request.timeZone")
    @Mapping(target = "sortOrder", source = "request.sortOrder")
    @Mapping(target = "labels", source = "labels")
    Task toEntity(
            TaskCreationRequest request,
            Project project,
            TaskSection section,
            Task parentTask,
            User createdBy,
            User assignee,
            Set<Label> labels
    );

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "section", source = "section")
    @Mapping(target = "parentTask", source = "parentTask")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "priority", source = "request.priority")
    @Mapping(target = "startAt", source = "request.startAt")
    @Mapping(target = "dueAt", source = "request.dueAt")
    @Mapping(target = "allDay", source = "request.allDay")
    @Mapping(target = "recurrenceRule", source = "request.recurrenceRule")
    @Mapping(target = "recurrenceMode", source = "request.recurrenceMode")
    @Mapping(target = "timeZone", source = "request.timeZone")
    @Mapping(target = "sortOrder", source = "request.sortOrder")
    @Mapping(target = "labels", source = "labels")
    void updateEntity(
            TaskUpdateRequest request,
            TaskSection section,
            Task parentTask,
            User assignee,
            Set<Label> labels,
            @MappingTarget Task task
    );

    default void updateStatus(TaskStatusUpdateRequest request, @MappingTarget Task task) {
        TaskStatus status = request.getStatus();
        task.setStatus(status);

        if (status == TaskStatus.COMPLETED) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
        } else {
            task.setCompletedAt(null);
        }
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "parentTaskId", source = "parentTask.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "startAt", source = "startAt")
    @Mapping(target = "dueAt", source = "dueAt")
    @Mapping(target = "allDay", source = "allDay")
    @Mapping(target = "recurrenceRule", source = "recurrenceRule")
    @Mapping(target = "recurrenceMode", source = "recurrenceMode")
    @Mapping(target = "timeZone", source = "timeZone")
    @Mapping(target = "completedAt", source = "completedAt")
    @Mapping(target = "archivedAt", source = "archivedAt")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "sortOrder", source = "sortOrder")
    @Mapping(target = "labelIds", expression = "java(toLabelIds(task.getLabels()))")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    TaskResponse toResponse(Task task);

    default Set<UUID> toLabelIds(Set<Label> labels) {
        if (labels == null || labels.isEmpty()) {
            return Set.of();
        }

        return labels.stream()
                .map(Label::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }
}
