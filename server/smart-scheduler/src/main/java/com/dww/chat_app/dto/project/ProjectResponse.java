package com.dww.chat_app.dto.project;

import com.dww.chat_app.entity.enums.ProjectViewType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class ProjectResponse {

    UUID id;

    UUID workspaceId;

    UUID createdById;

    String name;

    String description;

    String color;

    String icon;

    ProjectViewType viewType;

    int sortOrder;

    LocalDateTime archivedAt;

    LocalDateTime deletedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
