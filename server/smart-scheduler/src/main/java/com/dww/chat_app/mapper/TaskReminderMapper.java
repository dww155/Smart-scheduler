package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.task.TaskReminderCreationRequest;
import com.dww.chat_app.dto.task.TaskReminderResponse;
import com.dww.chat_app.dto.task.TaskReminderUpdateRequest;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskReminder;
import com.dww.chat_app.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = TodoMapperConfig.class)
public interface TaskReminderMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "task", source = "task")
    @Mapping(target = "recipient", source = "recipient")
    @Mapping(target = "remindAt", source = "request.remindAt")
    @Mapping(target = "timeZone", source = "request.timeZone")
    @Mapping(target = "channel", source = "request.channel")
    TaskReminder toEntity(TaskReminderCreationRequest request, Task task, User recipient);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "remindAt", source = "request.remindAt")
    @Mapping(target = "timeZone", source = "request.timeZone")
    @Mapping(target = "channel", source = "request.channel")
    void updateEntity(
            TaskReminderUpdateRequest request,
            @MappingTarget TaskReminder reminder
    );

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "recipientId", source = "recipient.id")
    @Mapping(target = "remindAt", source = "remindAt")
    @Mapping(target = "timeZone", source = "timeZone")
    @Mapping(target = "channel", source = "channel")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "sentAt", source = "sentAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    TaskReminderResponse toResponse(TaskReminder reminder);
}
