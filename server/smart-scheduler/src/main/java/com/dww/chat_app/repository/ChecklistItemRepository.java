package com.dww.chat_app.repository;

import com.dww.chat_app.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, UUID> {
    Optional<ChecklistItem> findByIdAndTaskId(UUID id, UUID taskId);

    List<ChecklistItem> findAllByTaskIdOrderBySortOrderAsc(UUID taskId);
}
