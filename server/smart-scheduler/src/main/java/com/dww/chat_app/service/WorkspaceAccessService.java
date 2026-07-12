package com.dww.chat_app.service;

import com.dww.chat_app.constant.UserConstant;
import com.dww.chat_app.entity.Role;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import com.dww.chat_app.entity.WorkspaceMember;
import com.dww.chat_app.entity.enums.WorkspaceMemberRole;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.repository.UserRepository;
import com.dww.chat_app.repository.WorkspaceMemberRepository;
import com.dww.chat_app.repository.WorkspaceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Centralizes workspace authorization so every resource beneath a workspace
 * applies the same visibility and role rules.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class WorkspaceAccessService {

    UserRepository userRepository;
    WorkspaceRepository workspaceRepository;
    WorkspaceMemberRepository workspaceMemberRepository;

    /**
     * Resolves the authenticated account from the database and rejects users
     * that were disabled or soft-deleted after their token was issued.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return userRepository.findByUsernameAndActiveTrueAndDeletedAtIsNull(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
    }

    public boolean isCurrentUserGlobalAdmin() {
        return hasGlobalAdminRole(getCurrentUser());
    }

    public Workspace getWorkspaceOrThrow(UUID workspaceId) {
        if (workspaceId == null) {
            throw new AppException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        return workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    public void requireReadable(Workspace workspace) {
        Workspace activeWorkspace = getActiveWorkspace(workspace);
        User currentUser = getCurrentUser();

        if (hasGlobalAdminRole(currentUser) || resolveRole(activeWorkspace, currentUser) != null) {
            return;
        }

        throw forbidden();
    }

    public void requireContributor(Workspace workspace) {
        Workspace activeWorkspace = getActiveWorkspace(workspace);
        User currentUser = getCurrentUser();

        if (hasGlobalAdminRole(currentUser)
                || hasAtLeastContributorRole(resolveRole(activeWorkspace, currentUser))) {
            return;
        }

        throw forbidden();
    }

    public void requireManager(Workspace workspace) {
        Workspace activeWorkspace = getActiveWorkspace(workspace);
        User currentUser = getCurrentUser();

        if (hasGlobalAdminRole(currentUser)
                || hasAtLeastManagerRole(resolveRole(activeWorkspace, currentUser))) {
            return;
        }

        throw forbidden();
    }

    public void requireOwner(Workspace workspace) {
        Workspace activeWorkspace = getActiveWorkspace(workspace);
        User currentUser = getCurrentUser();

        if (hasGlobalAdminRole(currentUser)
                || resolveRole(activeWorkspace, currentUser) == WorkspaceMemberRole.OWNER) {
            return;
        }

        throw forbidden();
    }

    public boolean isCurrentUser(UUID userId) {
        return userId != null && userId.equals(getCurrentUser().getId());
    }

    /**
     * Returns whether a user is visible as a workspace member. The direct
     * owner relation is intentionally included for workspaces created before
     * owner memberships were consistently persisted.
     */
    public boolean isMember(UUID workspaceId, UUID userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }

        return workspaceRepository.existsByIdAndOwnerIdAndDeletedAtIsNull(workspaceId, userId)
                || workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndWorkspaceDeletedAtIsNull(
                workspaceId,
                userId
        );
    }

    private Workspace getActiveWorkspace(Workspace workspace) {
        if (workspace == null || workspace.getId() == null) {
            throw new AppException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        return getWorkspaceOrThrow(workspace.getId());
    }

    private WorkspaceMemberRole resolveRole(Workspace workspace, User user) {
        if (workspace.getOwner() != null && user.getId().equals(workspace.getOwner().getId())) {
            return WorkspaceMemberRole.OWNER;
        }

        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())
                .map(WorkspaceMember::getRole)
                .orElse(null);
    }

    private boolean hasGlobalAdminRole(User user) {
        return user.getRoles() != null
                && user.getRoles().stream()
                .map(Role::getName)
                .filter(roleName -> roleName != null && !roleName.isBlank())
                .map(this::normalizeRoleName)
                .anyMatch(UserConstant.ROLE_ADMIN::equalsIgnoreCase);
    }

    private String normalizeRoleName(String roleName) {
        return roleName.regionMatches(true, 0, "ROLE_", 0, "ROLE_".length())
                ? roleName.substring("ROLE_".length())
                : roleName;
    }

    private boolean hasAtLeastContributorRole(WorkspaceMemberRole role) {
        return role == WorkspaceMemberRole.OWNER
                || role == WorkspaceMemberRole.ADMIN
                || role == WorkspaceMemberRole.MEMBER;
    }

    private boolean hasAtLeastManagerRole(WorkspaceMemberRole role) {
        return role == WorkspaceMemberRole.OWNER || role == WorkspaceMemberRole.ADMIN;
    }

    private AppException forbidden() {
        return new AppException(ErrorCode.FORBIDDEN);
    }
}
