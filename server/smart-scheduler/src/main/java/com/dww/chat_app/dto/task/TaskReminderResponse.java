package com.dww.chat_app.dto.task;

import com.dww.chat_app.entity.enums.ReminderChannel;
import com.dww.chat_app.entity.enums.ReminderStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class TaskReminderResponse {

    UUID id;

    UUID taskId;

    UUID recipientId;

    LocalDateTime remindAt;

    String timeZone;

    ReminderChannel channel;

    ReminderStatus status;

    LocalDateTime sentAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
