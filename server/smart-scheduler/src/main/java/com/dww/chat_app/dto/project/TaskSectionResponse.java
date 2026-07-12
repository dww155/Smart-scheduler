package com.dww.chat_app.dto.project;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class TaskSectionResponse {

    UUID id;

    UUID projectId;

    String name;

    String description;

    int sortOrder;

    LocalDateTime archivedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
