package com.dww.chat_app.dto.task;

import com.dww.chat_app.entity.enums.RecurrenceMode;
import com.dww.chat_app.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskCreationRequest {

    UUID sectionId;

    UUID parentTaskId;

    UUID assigneeId;

    @NotBlank(message = "INVALID_REQUEST")
    @Size(max = 500, message = "INVALID_REQUEST")
    String title;

    @Size(max = 20_000, message = "INVALID_REQUEST")
    String description;

    @NotNull(message = "INVALID_REQUEST")
    TaskPriority priority = TaskPriority.NONE;

    LocalDateTime startAt;

    LocalDateTime dueAt;

    @NotNull(message = "INVALID_REQUEST")
    Boolean allDay = true;

    @Size(max = 1000, message = "INVALID_REQUEST")
    String recurrenceRule;

    @NotNull(message = "INVALID_REQUEST")
    RecurrenceMode recurrenceMode = RecurrenceMode.NONE;

    @Size(max = 100, message = "INVALID_REQUEST")
    String timeZone;

    @NotNull(message = "INVALID_REQUEST")
    @PositiveOrZero(message = "INVALID_REQUEST")
    Integer sortOrder = 0;

    @NotNull(message = "INVALID_REQUEST")
    @Size(max = 100, message = "INVALID_REQUEST")
    Set<@NotNull(message = "INVALID_REQUEST") UUID> labelIds = new HashSet<>();
}
