package com.dww.chat_app.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChecklistItemCreationRequest {

    @NotBlank(message = "INVALID_REQUEST")
    @Size(max = 1000, message = "INVALID_REQUEST")
    String content;

    @NotNull(message = "INVALID_REQUEST")
    @PositiveOrZero(message = "INVALID_REQUEST")
    Integer sortOrder = 0;
}
