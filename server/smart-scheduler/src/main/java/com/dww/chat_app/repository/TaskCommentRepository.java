package com.dww.chat_app.repository;

import com.dww.chat_app.entity.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {
    Optional<TaskComment> findByIdAndTaskIdAndDeletedAtIsNull(UUID id, UUID taskId);

    Page<TaskComment> findAllByTaskIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID taskId,
            Pageable pageable
    );
}
