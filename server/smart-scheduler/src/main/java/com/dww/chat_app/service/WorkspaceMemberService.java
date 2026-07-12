package com.dww.chat_app.service;

import com.dww.chat_app.dto.workspace.WorkspaceMemberCreationRequest;
import com.dww.chat_app.dto.workspace.WorkspaceMemberResponse;
import com.dww.chat_app.dto.workspace.WorkspaceMemberRoleUpdateRequest;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import com.dww.chat_app.entity.WorkspaceMember;
import com.dww.chat_app.entity.enums.WorkspaceMemberRole;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.WorkspaceMemberMapper;
import com.dww.chat_app.repository.UserRepository;
import com.dww.chat_app.repository.WorkspaceMemberRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkspaceMemberService {

    WorkspaceMemberRepository workspaceMemberRepository;
    UserRepository userRepository;
    WorkspaceMemberMapper workspaceMemberMapper;
    WorkspaceAccessService workspaceAccessService;

    @Transactional
    public WorkspaceMemberResponse addMember(
            UUID workspaceId,
            WorkspaceMemberCreationRequest request
    ) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);
        rejectOwnerRole(request.getRole());

        User user = getActiveUser(request.getUserId());
        if (isWorkspaceOwner(workspace, user)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT);
        }

        WorkspaceMember member = workspaceMemberMapper.toEntity(request, workspace, user);
        return workspaceMemberMapper.toResponse(workspaceMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getMembers(UUID workspaceId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);

        return workspaceMemberRepository.findAllByWorkspaceIdOrderByJoinedAtAsc(workspaceId)
                .stream()
                .map(workspaceMemberMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceMemberResponse getMember(UUID workspaceId, UUID memberId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireReadable(workspace);

        return workspaceMemberMapper.toResponse(findMember(workspaceId, memberId));
    }

    @Transactional
    public WorkspaceMemberResponse updateMemberRole(
            UUID workspaceId,
            UUID memberId,
            WorkspaceMemberRoleUpdateRequest request
    ) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireOwner(workspace);

        WorkspaceMember member = findMember(workspaceId, memberId);
        if (isOwnerMembership(workspace, member)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        rejectOwnerRole(request.getRole());

        workspaceMemberMapper.updateRole(request, member);
        return workspaceMemberMapper.toResponse(workspaceMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID memberId) {
        Workspace workspace = workspaceAccessService.getWorkspaceOrThrow(workspaceId);
        workspaceAccessService.requireManager(workspace);

        WorkspaceMember member = findMember(workspaceId, memberId);
        if (isOwnerMembership(workspace, member)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        workspaceMemberRepository.delete(member);
    }

    private User getActiveUser(UUID userId) {
        if (userId == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return userRepository.findById(userId)
                .filter(user -> user.isActive() && user.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private WorkspaceMember findMember(UUID workspaceId, UUID memberId) {
        if (memberId == null) {
            throw new AppException(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND);
        }

        return workspaceMemberRepository.findByIdAndWorkspaceId(memberId, workspaceId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND));
    }

    private void rejectOwnerRole(WorkspaceMemberRole role) {
        if (role == null || role == WorkspaceMemberRole.OWNER) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private boolean isWorkspaceOwner(Workspace workspace, User user) {
        return workspace.getOwner() != null
                && workspace.getOwner().getId().equals(user.getId());
    }

    private boolean isOwnerMembership(Workspace workspace, WorkspaceMember member) {
        return member.getRole() == WorkspaceMemberRole.OWNER
                || isWorkspaceOwner(workspace, member.getUser());
    }
}
