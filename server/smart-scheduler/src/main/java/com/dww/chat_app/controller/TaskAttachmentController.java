package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.task.TaskAttachmentCreationRequest;
import com.dww.chat_app.dto.task.TaskAttachmentDownload;
import com.dww.chat_app.dto.task.TaskAttachmentResponse;
import com.dww.chat_app.dto.task.TaskAttachmentUpdateRequest;
import com.dww.chat_app.service.TaskAttachmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks/{taskId}/attachments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskAttachmentController {

    TaskAttachmentService taskAttachmentService;

    @GetMapping
    public ApiResponse<List<TaskAttachmentResponse>> getAttachments(
            @PathVariable("taskId") UUID taskId
    ) {
        return ApiResponse.success(taskAttachmentService.getAttachments(taskId));
    }

    @GetMapping("/{attachmentId}")
    public ApiResponse<TaskAttachmentResponse> getAttachment(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("attachmentId") UUID attachmentId
    ) {
        return ApiResponse.success(taskAttachmentService.getAttachment(taskId, attachmentId));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("attachmentId") UUID attachmentId
    ) {
        TaskAttachmentDownload download = taskAttachmentService.downloadAttachment(taskId, attachmentId);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(safeFileName(download.getOriginalFileName()), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(resolveContentType(download.getContentType()))
                .contentLength(download.getSizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(download.getResource());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TaskAttachmentResponse> createAttachment(
            @PathVariable("taskId") UUID taskId,
            @Valid @ModelAttribute TaskAttachmentCreationRequest request
    ) {
        return ApiResponse.success(
                "Create attachment successfully",
                taskAttachmentService.createAttachment(taskId, request)
        );
    }

    @PutMapping(value = "/{attachmentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TaskAttachmentResponse> updateAttachment(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("attachmentId") UUID attachmentId,
            @Valid @ModelAttribute TaskAttachmentUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update attachment successfully",
                taskAttachmentService.updateAttachment(taskId, attachmentId, request)
        );
    }

    @DeleteMapping("/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("attachmentId") UUID attachmentId
    ) {
        taskAttachmentService.deleteAttachment(taskId, attachmentId);

        return ApiResponse.success("Delete attachment successfully");
    }

    private MediaType resolveContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String safeFileName(String originalFileName) {
        return originalFileName == null || originalFileName.isBlank() ? "download" : originalFileName;
    }
}
