package com.dww.chat_app.service;

import com.dww.chat_app.entity.enums.RecurrenceMode;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Keeps task scheduling rules in one place so create and update operations
 * apply exactly the same business validation.
 */
@Component
public class TaskValidationService {

    public void validateTaskSchedule(
            LocalDateTime startAt,
            LocalDateTime dueAt,
            RecurrenceMode recurrenceMode,
            String recurrenceRule,
            String timeZone
    ) {
        if (startAt != null && dueAt != null && startAt.isAfter(dueAt)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        boolean hasRecurrenceRule = recurrenceRule != null && !recurrenceRule.isBlank();
        if ((recurrenceMode == RecurrenceMode.NONE && hasRecurrenceRule)
                || (recurrenceMode != RecurrenceMode.NONE && !hasRecurrenceRule)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        validateTimeZone(timeZone);
    }

    public void validateTimeZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            return;
        }

        try {
            ZoneId.of(timeZone);
        } catch (DateTimeException exception) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}
