package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.task.ChecklistItemCreationRequest;
import com.dww.chat_app.dto.task.ChecklistItemResponse;
import com.dww.chat_app.dto.task.ChecklistItemStatusUpdateRequest;
import com.dww.chat_app.dto.task.ChecklistItemUpdateRequest;
import com.dww.chat_app.entity.ChecklistItem;
import com.dww.chat_app.entity.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;

@Mapper(config = TodoMapperConfig.class)
public interface ChecklistItemMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "task", source = "task")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "sortOrder", source = "request.sortOrder")
    ChecklistItem toEntity(ChecklistItemCreationRequest request, Task task);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "sortOrder", source = "request.sortOrder")
    void updateEntity(
            ChecklistItemUpdateRequest request,
            @MappingTarget ChecklistItem checklistItem
    );

    default void updateStatus(
            ChecklistItemStatusUpdateRequest request,
            @MappingTarget ChecklistItem checklistItem
    ) {
        boolean completed = request.getCompleted();
        checklistItem.setCompleted(completed);

        if (completed) {
            if (checklistItem.getCompletedAt() == null) {
                checklistItem.setCompletedAt(LocalDateTime.now());
            }
        } else {
            checklistItem.setCompletedAt(null);
        }
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "completed", source = "completed")
    @Mapping(target = "completedAt", source = "completedAt")
    @Mapping(target = "sortOrder", source = "sortOrder")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    ChecklistItemResponse toResponse(ChecklistItem checklistItem);
}
