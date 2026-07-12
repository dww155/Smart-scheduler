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

    String username;

    String email;

    boolean active;

    WorkspaceMemberRole role;

    LocalDateTime joinedAt;

    LocalDateTime updatedAt;

    Long version;
}
