package com.dww.chat_app.dto.project;

import com.dww.chat_app.entity.enums.ProjectViewType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectUpdateRequest {

    @NotBlank(message = "INVALID_REQUEST")
    @Size(max = 150, message = "INVALID_REQUEST")
    String name;

    @Size(max = 20_000, message = "INVALID_REQUEST")
    String description;

    @Size(max = 20, message = "INVALID_REQUEST")
    String color;

    @Size(max = 50, message = "INVALID_REQUEST")
    String icon;

    @NotNull(message = "INVALID_REQUEST")
    ProjectViewType viewType;

    @NotNull(message = "INVALID_REQUEST")
    @PositiveOrZero(message = "INVALID_REQUEST")
    Integer sortOrder;
}
