package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.task.TaskAttachmentCreationRequest;
import com.dww.chat_app.dto.task.TaskAttachmentUpdateRequest;
import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.TaskAttachment;
import com.dww.chat_app.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.web.multipart.MultipartFile;

@Mapper(config = TodoMapperConfig.class)
public interface TaskAttachmentMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "task", source = "task")
    @Mapping(target = "uploadedBy", source = "uploadedBy")
    @Mapping(
            target = "originalFileName",
            expression = "java(extractOriginalFileName(request.getFile()))"
    )
    @Mapping(target = "storageKey", source = "storageKey")
    @Mapping(
            target = "contentType",
            expression = "java(extractContentType(request.getFile()))"
    )
    @Mapping(target = "sizeBytes", expression = "java(request.getFile().getSize())")
    TaskAttachment toEntity(
            TaskAttachmentCreationRequest request,
            Task task,
            User uploadedBy,
            String storageKey
    );

    @BeanMapping(ignoreByDefault = true)
    @Mapping(
            target = "originalFileName",
            expression = "java(extractOriginalFileName(request.getFile()))"
    )
    @Mapping(target = "storageKey", source = "storageKey")
    @Mapping(
            target = "contentType",
            expression = "java(extractContentType(request.getFile()))"
    )
    @Mapping(target = "sizeBytes", expression = "java(request.getFile().getSize())")
    void updateEntity(
            TaskAttachmentUpdateRequest request,
            String storageKey,
            @MappingTarget TaskAttachment attachment
    );

    default String extractOriginalFileName(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return "file";
        }

        String fileName = file.getOriginalFilename().replace('\\', '/');
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1).trim();

        return fileName.isEmpty() ? "file" : fileName;
    }

    default String extractContentType(MultipartFile file) {
        if (file == null || file.getContentType() == null || file.getContentType().isBlank()) {
            return "application/octet-stream";
        }

        return file.getContentType();
    }
}
