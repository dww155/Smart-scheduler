package com.dww.chat_app.dto.label;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class LabelResponse {

    UUID id;

    UUID workspaceId;

    UUID createdById;

    String name;

    String color;

    LocalDateTime archivedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
