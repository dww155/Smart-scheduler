package com.dww.chat_app.dto.task;

import com.dww.chat_app.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskStatusUpdateRequest {

    @NotNull(message = "INVALID_STATUS")
    TaskStatus status;
}
