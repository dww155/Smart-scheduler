package com.dww.chat_app.dto.task;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class TaskAttachmentResponse {

    UUID id;

    UUID taskId;

    UUID uploadedById;

    String originalFileName;

    String contentType;

    Long sizeBytes;

    LocalDateTime deletedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
