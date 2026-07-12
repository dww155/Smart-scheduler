package com.dww.chat_app.dto.workspace;

import com.dww.chat_app.entity.enums.WorkspaceType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class WorkspaceResponse {

    UUID id;

    UUID ownerId;

    String name;

    String description;

    WorkspaceType type;

    String color;

    LocalDateTime archivedAt;

    LocalDateTime deletedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
