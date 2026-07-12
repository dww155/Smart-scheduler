package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.PageResponse;
import com.dww.chat_app.dto.task.TaskCommentCreationRequest;
import com.dww.chat_app.dto.task.TaskCommentResponse;
import com.dww.chat_app.dto.task.TaskCommentUpdateRequest;
import com.dww.chat_app.service.TaskCommentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/tasks/{taskId}/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskCommentController {

    TaskCommentService taskCommentService;

    @GetMapping
    public ApiResponse<PageResponse<TaskCommentResponse>> getComments(
            @PathVariable("taskId") UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(taskCommentService.getComments(taskId, page, size));
    }

    @GetMapping("/{commentId}")
    public ApiResponse<TaskCommentResponse> getComment(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("commentId") UUID commentId
    ) {
        return ApiResponse.success(taskCommentService.getComment(taskId, commentId));
    }

    @PostMapping
    public ApiResponse<TaskCommentResponse> createComment(
            @PathVariable("taskId") UUID taskId,
            @Valid @RequestBody TaskCommentCreationRequest request
    ) {
        return ApiResponse.success(
                "Create comment successfully",
                taskCommentService.createComment(taskId, request)
        );
    }

    @PutMapping("/{commentId}")
    public ApiResponse<TaskCommentResponse> updateComment(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("commentId") UUID commentId,
            @Valid @RequestBody TaskCommentUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update comment successfully",
                taskCommentService.updateComment(taskId, commentId, request)
        );
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("commentId") UUID commentId
    ) {
        taskCommentService.deleteComment(taskId, commentId);

        return ApiResponse.success("Delete comment successfully");
    }
}
