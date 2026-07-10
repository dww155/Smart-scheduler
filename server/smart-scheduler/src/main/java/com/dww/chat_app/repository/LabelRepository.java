package com.dww.chat_app.repository;

import com.dww.chat_app.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<Label, UUID> {
    Optional<Label> findByIdAndWorkspaceId(UUID id, UUID workspaceId);

    List<Label> findAllByWorkspaceIdAndArchivedAtIsNullOrderByNameAsc(UUID workspaceId);
}
