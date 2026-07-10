package com.dww.chat_app.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskCommentCreationRequest {

    @NotBlank(message = "INVALID_REQUEST")
    @Size(max = 15_000, message = "INVALID_REQUEST")
    String content;
}
