package com.dww.chat_app.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChecklistItemStatusUpdateRequest {

    @NotNull(message = "INVALID_STATUS")
    Boolean completed;
}
