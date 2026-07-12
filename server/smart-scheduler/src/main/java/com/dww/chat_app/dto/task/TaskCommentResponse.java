package com.dww.chat_app.dto.task;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class TaskCommentResponse {

    UUID id;

    UUID taskId;

    UUID authorId;

    String content;

    LocalDateTime deletedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
