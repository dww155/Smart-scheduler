package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.workspace.WorkspaceMemberCreationRequest;
import com.dww.chat_app.dto.workspace.WorkspaceMemberResponse;
import com.dww.chat_app.dto.workspace.WorkspaceMemberRoleUpdateRequest;
import com.dww.chat_app.dto.workspace.MemberCandidateResponse;
import com.dww.chat_app.service.WorkspaceMemberService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkspaceMemberController {

    WorkspaceMemberService workspaceMemberService;

    @PostMapping
    public ApiResponse<WorkspaceMemberResponse> addMember(
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody WorkspaceMemberCreationRequest request
    ) {
        return ApiResponse.success(
                "Workspace member added successfully",
                workspaceMemberService.addMember(workspaceId, request)
        );
    }

    @GetMapping
    public ApiResponse<List<WorkspaceMemberResponse>> getMembers(
            @PathVariable("workspaceId") UUID workspaceId
    ) {
        return ApiResponse.success(workspaceMemberService.getMembers(workspaceId));
    }

    @GetMapping("/candidates")
    public ApiResponse<List<MemberCandidateResponse>> getMemberCandidates(
            @PathVariable("workspaceId") UUID workspaceId,
            @RequestParam(defaultValue = "") String q
    ) {
        return ApiResponse.success(workspaceMemberService.getMemberCandidates(workspaceId, q));
    }

    @GetMapping("/{memberId}")
    public ApiResponse<WorkspaceMemberResponse> getMember(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("memberId") UUID memberId
    ) {
        return ApiResponse.success(workspaceMemberService.getMember(workspaceId, memberId));
    }

    @PatchMapping("/{memberId}/role")
    public ApiResponse<WorkspaceMemberResponse> updateMemberRole(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("memberId") UUID memberId,
            @Valid @RequestBody WorkspaceMemberRoleUpdateRequest request
    ) {
        return ApiResponse.success(
                "Workspace member role updated successfully",
                workspaceMemberService.updateMemberRole(workspaceId, memberId, request)
        );
    }

    @DeleteMapping("/{memberId}")
    public ApiResponse<Void> removeMember(
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("memberId") UUID memberId
    ) {
        workspaceMemberService.removeMember(workspaceId, memberId);
        return ApiResponse.success("Workspace member removed successfully");
    }
}
