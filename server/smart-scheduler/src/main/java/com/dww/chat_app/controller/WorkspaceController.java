package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.workspace.WorkspaceCreationRequest;
import com.dww.chat_app.dto.workspace.WorkspaceResponse;
import com.dww.chat_app.dto.workspace.WorkspaceUpdateRequest;
import com.dww.chat_app.service.WorkspaceService;
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
@RequestMapping("/workspaces")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkspaceController {

    WorkspaceService workspaceService;

    @PostMapping
    public ApiResponse<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody WorkspaceCreationRequest request
    ) {
        return ApiResponse.success(
                "Workspace created successfully",
                workspaceService.createWorkspace(request)
        );
    }

    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> getWorkspaces() {
        return ApiResponse.success(workspaceService.getWorkspaces());
    }

    @GetMapping("/{workspaceId}")
    public ApiResponse<WorkspaceResponse> getWorkspace(
            @PathVariable("workspaceId") UUID workspaceId
    ) {
        return ApiResponse.success(workspaceService.getWorkspace(workspaceId));
    }

    @PutMapping("/{workspaceId}")
    public ApiResponse<WorkspaceResponse> updateWorkspace(
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody WorkspaceUpdateRequest request
    ) {
        return ApiResponse.success(
                "Workspace updated successfully",
                workspaceService.updateWorkspace(workspaceId, request)
        );
    }

    @DeleteMapping("/{workspaceId}")
    public ApiResponse<Void> deleteWorkspace(@PathVariable("workspaceId") UUID workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ApiResponse.success("Workspace deleted successfully");
    }
}
