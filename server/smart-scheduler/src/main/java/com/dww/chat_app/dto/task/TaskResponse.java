package com.dww.chat_app.dto.task;

import com.dww.chat_app.entity.enums.RecurrenceMode;
import com.dww.chat_app.entity.enums.TaskPriority;
import com.dww.chat_app.entity.enums.TaskStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class TaskResponse {

    UUID id;

    UUID projectId;

    UUID sectionId;

    UUID parentTaskId;

    UUID createdById;

    UUID assigneeId;

    String title;

    String description;

    TaskStatus status;

    TaskPriority priority;

    LocalDateTime startAt;

    LocalDateTime dueAt;

    boolean allDay;

    String recurrenceRule;

    RecurrenceMode recurrenceMode;

    String timeZone;

    LocalDateTime completedAt;

    LocalDateTime archivedAt;

    LocalDateTime deletedAt;

    int sortOrder;

    Set<UUID> labelIds;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    Long version;
}
