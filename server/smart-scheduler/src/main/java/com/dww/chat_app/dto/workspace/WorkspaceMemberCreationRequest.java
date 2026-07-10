package com.dww.chat_app.dto.workspace;

import com.dww.chat_app.entity.enums.WorkspaceMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceMemberCreationRequest {

    @NotNull(message = "INVALID_REQUEST")
    UUID userId;

    @NotNull(message = "INVALID_REQUEST")
    WorkspaceMemberRole role = WorkspaceMemberRole.MEMBER;
}
