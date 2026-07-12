package com.dww.chat_app.dto.task;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class ChecklistItemResponse {

    UUID id;

    UUID taskId;

    String content;

    boolean completed;

    LocalDateTime completedAt;

    int sortOrder;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
