package com.dww.chat_app.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskSectionCreationRequest {

    @NotBlank(message = "INVALID_REQUEST")
    @Size(max = 150, message = "INVALID_REQUEST")
    String name;

    @Size(max = 500, message = "INVALID_REQUEST")
    String description;

    @NotNull(message = "INVALID_REQUEST")
    @PositiveOrZero(message = "INVALID_REQUEST")
    Integer sortOrder = 0;
}
