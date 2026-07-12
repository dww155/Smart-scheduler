package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.label.LabelCreationRequest;
import com.dww.chat_app.dto.label.LabelResponse;
import com.dww.chat_app.dto.label.LabelUpdateRequest;
import com.dww.chat_app.service.LabelService;
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
@RequestMapping("/workspaces/{workspaceId}/labels")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LabelController {

    LabelService labelService;

    @PostMapping
    public ApiResponse<LabelResponse> createLabel(
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody LabelCreationRequest request
    ) {
        return ApiResponse.success(
                "Label created successfully",
                labelService.createLabel(workspaceId, request)
        );
    }

    @GetMapping
    public ApiResponse<List<LabelResponse>> getLabels(@PathVariable("workspaceId") UUID workspaceId) {
        return ApiResponse.success(labelService.getLabels(workspaceId));
    }

    @GetMapping("/{labelId}")
    public ApiResponse<LabelResponse> getLabel(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("labelId") UUID labelId
    ) {
        return ApiResponse.success(labelService.getLabel(workspaceId, labelId));
    }

    @PutMapping("/{labelId}")
    public ApiResponse<LabelResponse> updateLabel(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("labelId") UUID labelId,
            @Valid @RequestBody LabelUpdateRequest request
    ) {
        return ApiResponse.success(
                "Label updated successfully",
                labelService.updateLabel(workspaceId, labelId, request)
        );
    }

    @DeleteMapping("/{labelId}")
    public ApiResponse<Void> archiveLabel(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("labelId") UUID labelId
    ) {
        labelService.archiveLabel(workspaceId, labelId);
        return ApiResponse.success("Label archived successfully");
    }
}
