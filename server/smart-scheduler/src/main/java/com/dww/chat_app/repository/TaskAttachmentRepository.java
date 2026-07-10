package com.dww.chat_app.repository;

import com.dww.chat_app.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {
    Optional<TaskAttachment> findByIdAndTaskIdAndDeletedAtIsNull(UUID id, UUID taskId);

    List<TaskAttachment> findAllByTaskIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID taskId);
}
