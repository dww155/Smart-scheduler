package com.dww.chat_app.repository;

import com.dww.chat_app.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByIdAndDeletedAtIsNull(UUID id);

    List<Project> findAllByWorkspaceIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID workspaceId);
}
