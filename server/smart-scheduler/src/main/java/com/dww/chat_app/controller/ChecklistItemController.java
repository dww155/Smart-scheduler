package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.task.ChecklistItemCreationRequest;
import com.dww.chat_app.dto.task.ChecklistItemResponse;
import com.dww.chat_app.dto.task.ChecklistItemStatusUpdateRequest;
import com.dww.chat_app.dto.task.ChecklistItemUpdateRequest;
import com.dww.chat_app.service.ChecklistItemService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks/{taskId}/checklist-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChecklistItemController {

    ChecklistItemService checklistItemService;

    @GetMapping
    public ApiResponse<List<ChecklistItemResponse>> getChecklistItems(
            @PathVariable("taskId") UUID taskId
    ) {
        return ApiResponse.success(checklistItemService.getChecklistItems(taskId));
    }

    @GetMapping("/{checklistItemId}")
    public ApiResponse<ChecklistItemResponse> getChecklistItem(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("checklistItemId") UUID checklistItemId
    ) {
        return ApiResponse.success(checklistItemService.getChecklistItem(taskId, checklistItemId));
    }

    @PostMapping
    public ApiResponse<ChecklistItemResponse> createChecklistItem(
            @PathVariable("taskId") UUID taskId,
            @Valid @RequestBody ChecklistItemCreationRequest request
    ) {
        return ApiResponse.success(
                "Create checklist item successfully",
                checklistItemService.createChecklistItem(taskId, request)
        );
    }

    @PutMapping("/{checklistItemId}")
    public ApiResponse<ChecklistItemResponse> updateChecklistItem(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("checklistItemId") UUID checklistItemId,
            @Valid @RequestBody ChecklistItemUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update checklist item successfully",
                checklistItemService.updateChecklistItem(taskId, checklistItemId, request)
        );
    }

    @PatchMapping("/{checklistItemId}/status")
    public ApiResponse<ChecklistItemResponse> updateChecklistItemStatus(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("checklistItemId") UUID checklistItemId,
            @Valid @RequestBody ChecklistItemStatusUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update checklist item status successfully",
                checklistItemService.updateChecklistItemStatus(taskId, checklistItemId, request)
        );
    }

    @DeleteMapping("/{checklistItemId}")
    public ApiResponse<Void> deleteChecklistItem(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("checklistItemId") UUID checklistItemId
    ) {
        checklistItemService.deleteChecklistItem(taskId, checklistItemId);

        return ApiResponse.success("Delete checklist item successfully");
    }
}
