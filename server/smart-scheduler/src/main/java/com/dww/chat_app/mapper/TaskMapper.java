package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.task.TaskCreationRequest;
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
import java.util.Set;

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
}
