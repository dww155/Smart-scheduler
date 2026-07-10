package com.dww.chat_app.dto.workspace;

import com.dww.chat_app.entity.enums.WorkspaceMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceMemberRoleUpdateRequest {

    @NotNull(message = "INVALID_REQUEST")
    WorkspaceMemberRole role;
}
