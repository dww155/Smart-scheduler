package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.task.TaskCreationRequest;
import com.dww.chat_app.dto.task.TaskResponse;
import com.dww.chat_app.dto.task.TaskStatusUpdateRequest;
import com.dww.chat_app.dto.task.TaskUpdateRequest;
import com.dww.chat_app.service.TaskService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskController {

    TaskService taskService;

    @GetMapping("/tasks")
    public ApiResponse<List<TaskResponse>> getVisibleTasks() {
        return ApiResponse.success(taskService.getVisibleTasks());
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ApiResponse<List<TaskResponse>> getTasksByProject(@PathVariable UUID projectId) {
        return ApiResponse.success(taskService.getTasksByProject(projectId));
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ApiResponse<TaskResponse> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody TaskCreationRequest request
    ) {
        return ApiResponse.success("Create task successfully", taskService.createTask(projectId, request));
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<TaskResponse> getTask(@PathVariable UUID taskId) {
        return ApiResponse.success(taskService.getTask(taskId));
    }

    @PutMapping("/tasks/{taskId}")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        return ApiResponse.success("Update task successfully", taskService.updateTask(taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ApiResponse<Void> deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
        return ApiResponse.success("Delete task successfully");
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ApiResponse<TaskResponse> updateTaskStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskStatusUpdateRequest request
    ) {
        return ApiResponse.success("Update task status successfully", taskService.updateTaskStatus(taskId, request));
    }

    @GetMapping("/tasks/{taskId}/subtasks")
    public ApiResponse<List<TaskResponse>> getSubtasks(@PathVariable UUID taskId) {
        return ApiResponse.success(taskService.getSubtasks(taskId));
    }
}
