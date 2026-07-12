package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.project.TaskSectionCreationRequest;
import com.dww.chat_app.dto.project.TaskSectionResponse;
import com.dww.chat_app.dto.project.TaskSectionUpdateRequest;
import com.dww.chat_app.service.TaskSectionService;
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
@RequestMapping("/workspaces/{workspaceId}/projects/{projectId}/sections")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskSectionController {

    TaskSectionService taskSectionService;

    @PostMapping
    public ApiResponse<TaskSectionResponse> createSection(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("projectId") UUID projectId,
            @Valid @RequestBody TaskSectionCreationRequest request
    ) {
        return ApiResponse.success(
                "Task section created successfully",
                taskSectionService.createSection(workspaceId, projectId, request)
        );
    }

    @GetMapping
    public ApiResponse<List<TaskSectionResponse>> getSections(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("projectId") UUID projectId
    ) {
        return ApiResponse.success(taskSectionService.getSections(workspaceId, projectId));
    }

    @GetMapping("/{sectionId}")
    public ApiResponse<TaskSectionResponse> getSection(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("projectId") UUID projectId,
            @PathVariable("sectionId") UUID sectionId
    ) {
        return ApiResponse.success(taskSectionService.getSection(workspaceId, projectId, sectionId));
    }

    @PutMapping("/{sectionId}")
    public ApiResponse<TaskSectionResponse> updateSection(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("projectId") UUID projectId,
            @PathVariable("sectionId") UUID sectionId,
            @Valid @RequestBody TaskSectionUpdateRequest request
    ) {
        return ApiResponse.success(
                "Task section updated successfully",
                taskSectionService.updateSection(workspaceId, projectId, sectionId, request)
        );
    }

    @DeleteMapping("/{sectionId}")
    public ApiResponse<Void> archiveSection(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("projectId") UUID projectId,
            @PathVariable("sectionId") UUID sectionId
    ) {
        taskSectionService.archiveSection(workspaceId, projectId, sectionId);
        return ApiResponse.success("Task section archived successfully");
    }
}
