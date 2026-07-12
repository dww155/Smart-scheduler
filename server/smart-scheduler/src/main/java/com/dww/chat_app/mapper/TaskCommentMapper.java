package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.task.TaskCommentCreationRequest;
import com.dww.chat_app.dto.task.TaskCommentResponse;
import com.dww.chat_app.dto.task.TaskCommentUpdateRequest;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskComment;
import com.dww.chat_app.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = TodoMapperConfig.class)
public interface TaskCommentMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "task", source = "task")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "content", source = "request.content")
    TaskComment toEntity(TaskCommentCreationRequest request, Task task, User author);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "content", source = "request.content")
    void updateEntity(TaskCommentUpdateRequest request, @MappingTarget TaskComment comment);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    TaskCommentResponse toResponse(TaskComment comment);
}
