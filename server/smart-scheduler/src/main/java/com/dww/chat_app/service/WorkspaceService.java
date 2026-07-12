package com.dww.chat_app.service;

import com.dww.chat_app.dto.workspace.WorkspaceCreationRequest;
import com.dww.chat_app.dto.workspace.WorkspaceResponse;
import com.dww.chat_app.dto.workspace.WorkspaceUpdateRequest;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import com.dww.chat_app.entity.WorkspaceMember;
import com.dww.chat_app.entity.enums.WorkspaceMemberRole;
import com.dww.chat_app.mapper.WorkspaceMapper;
import com.dww.chat_app.repository.WorkspaceMemberRepository;
import com.dww.chat_app.repository.WorkspaceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkspaceService {

    WorkspaceRepository workspaceRepository;
    WorkspaceMemberRepository workspaceMemberRepository;
    WorkspaceMapper workspaceMapper;
    WorkspaceAccessService workspaceAccessService;

    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreationRequest request) {
        User currentUser = workspaceAccessService.getCurrentUser();
        Workspace workspace = workspaceMapper.toEntity(request, currentUser);
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember ownerMembership = WorkspaceMember.builder()
                .workspace(savedWorkspace)
                .user(currentUser)
                .role(WorkspaceMemberRole.OWNER)
                .build();
        workspaceMemberRepository.save(ownerMembership);

        return workspaceMapper.toResponse(savedWorkspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspaces() {
        User currentUser = workspaceAccessService.getCurrentUser();
        List<Workspace> workspaces = workspaceAccessService.isCurrentUserGlobalAdmin()
                ? workspaceRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                : workspaceRepository.findAllVisibleToUserId(currentUser.getId());

        return workspaces.stream()
                .map(workspaceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(UUID workspaceId, WorkspaceUpdateRequest request) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);

        workspaceMapper.updateEntity(request, workspace);
        return workspaceMapper.toResponse(workspaceRepository.save(workspace));
    }

    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireOwner(workspace);

        workspace.setDeletedAt(LocalDateTime.now());
        workspaceRepository.save(workspace);
    }
}
