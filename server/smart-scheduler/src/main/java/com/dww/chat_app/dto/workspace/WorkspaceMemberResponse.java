package com.dww.chat_app.dto.workspace;

import com.dww.chat_app.entity.enums.WorkspaceMemberRole;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class WorkspaceMemberResponse {

    UUID id;

    UUID workspaceId;

    UUID userId;

    WorkspaceMemberRole role;

    LocalDateTime joinedAt;

    LocalDateTime updatedAt;

    Long version;
}
