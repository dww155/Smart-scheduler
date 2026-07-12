package com.dww.chat_app.repository;

import com.dww.chat_app.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    Optional<WorkspaceMember> findByIdAndWorkspaceId(UUID id, UUID workspaceId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    boolean existsByWorkspaceIdAndUserIdAndWorkspaceDeletedAtIsNull(UUID workspaceId, UUID userId);

    List<WorkspaceMember> findAllByWorkspaceIdOrderByJoinedAtAsc(UUID workspaceId);
}
