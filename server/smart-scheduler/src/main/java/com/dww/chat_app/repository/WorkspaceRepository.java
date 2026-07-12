package com.dww.chat_app.repository;

import com.dww.chat_app.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByIdAndOwnerIdAndDeletedAtIsNull(UUID id, UUID ownerId);

    List<Workspace> findAllByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID ownerId);

    List<Workspace> findDistinctByMembersUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

    List<Workspace> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    @Query("""
            select distinct workspace
            from Workspace workspace
            left join workspace.members member
            where workspace.deletedAt is null
              and (workspace.owner.id = :userId or member.user.id = :userId)
            order by workspace.createdAt desc
            """)
    List<Workspace> findAllVisibleToUserId(@Param("userId") UUID userId);
}
