package com.dww.chat_app.dto.task;

import com.dww.chat_app.entity.enums.ReminderChannel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskReminderCreationRequest {

    @NotNull(message = "INVALID_REQUEST")
    @Future(message = "INVALID_REQUEST")
    LocalDateTime remindAt;

    @Size(max = 100, message = "INVALID_REQUEST")
    String timeZone;

    @NotNull(message = "INVALID_REQUEST")
    ReminderChannel channel = ReminderChannel.IN_APP;
}
