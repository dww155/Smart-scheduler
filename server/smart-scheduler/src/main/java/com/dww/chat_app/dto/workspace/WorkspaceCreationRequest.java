package com.dww.chat_app.dto.workspace;

import com.dww.chat_app.entity.enums.WorkspaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceCreationRequest {

    @NotBlank(message = "INVALID_REQUEST")
    @Size(max = 100, message = "INVALID_REQUEST")
    String name;

    @Size(max = 10000, message = "INVALID_REQUEST")
    String description;

    @NotNull(message = "INVALID_REQUEST")
    WorkspaceType type = WorkspaceType.PERSONAL;

    @Size(max = 20, message = "INVALID_REQUEST")
    String color;
}
