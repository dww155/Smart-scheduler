package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.task.TaskCommentCreationRequest;
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
}
