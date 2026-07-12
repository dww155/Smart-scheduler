package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.project.ProjectCreationRequest;
import com.dww.chat_app.dto.project.ProjectResponse;
import com.dww.chat_app.dto.project.ProjectUpdateRequest;
import com.dww.chat_app.service.ProjectService;
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
@RequestMapping("/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {

    ProjectService projectService;

    @PostMapping
    public ApiResponse<ProjectResponse> createProject(
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody ProjectCreationRequest request
    ) {
        return ApiResponse.success(
                "Project created successfully",
                projectService.createProject(workspaceId, request)
        );
    }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> getProjects(
            @PathVariable("workspaceId") UUID workspaceId
    ) {
        return ApiResponse.success(projectService.getProjects(workspaceId));
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponse> getProject(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("projectId") UUID projectId
    ) {
        return ApiResponse.success(projectService.getProject(workspaceId, projectId));
    }

    @PutMapping("/{projectId}")
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("projectId") UUID projectId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        return ApiResponse.success(
                "Project updated successfully",
                projectService.updateProject(workspaceId, projectId, request)
        );
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("projectId") UUID projectId
    ) {
        projectService.deleteProject(workspaceId, projectId);
        return ApiResponse.success("Project deleted successfully");
    }
}
