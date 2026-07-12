package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.task.TaskReminderCreationRequest;
import com.dww.chat_app.dto.task.TaskReminderResponse;
import com.dww.chat_app.dto.task.TaskReminderUpdateRequest;
import com.dww.chat_app.service.TaskReminderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks/{taskId}/reminders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskReminderController {

    TaskReminderService taskReminderService;

    @GetMapping
    public ApiResponse<List<TaskReminderResponse>> getReminders(
            @PathVariable("taskId") UUID taskId
    ) {
        return ApiResponse.success(taskReminderService.getReminders(taskId));
    }

    @GetMapping("/{reminderId}")
    public ApiResponse<TaskReminderResponse> getReminder(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("reminderId") UUID reminderId
    ) {
        return ApiResponse.success(taskReminderService.getReminder(taskId, reminderId));
    }

    @PostMapping
    public ApiResponse<TaskReminderResponse> createReminder(
            @PathVariable("taskId") UUID taskId,
            @Valid @RequestBody TaskReminderCreationRequest request
    ) {
        return ApiResponse.success(
                "Create reminder successfully",
                taskReminderService.createReminder(taskId, request)
        );
    }

    @PutMapping("/{reminderId}")
    public ApiResponse<TaskReminderResponse> updateReminder(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("reminderId") UUID reminderId,
            @Valid @RequestBody TaskReminderUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update reminder successfully",
                taskReminderService.updateReminder(taskId, reminderId, request)
        );
    }

    @DeleteMapping("/{reminderId}")
    public ApiResponse<Void> deleteReminder(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("reminderId") UUID reminderId
    ) {
        taskReminderService.deleteReminder(taskId, reminderId);

        return ApiResponse.success("Cancel reminder successfully");
    }
}
