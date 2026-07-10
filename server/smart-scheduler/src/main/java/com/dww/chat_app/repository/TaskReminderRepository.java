package com.dww.chat_app.repository;

import com.dww.chat_app.entity.TaskReminder;
import com.dww.chat_app.entity.enums.ReminderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskReminderRepository extends JpaRepository<TaskReminder, UUID> {
    Optional<TaskReminder> findByIdAndTaskId(UUID id, UUID taskId);

    List<TaskReminder> findAllByTaskIdAndRecipientIdOrderByRemindAtAsc(UUID taskId, UUID recipientId);

    Page<TaskReminder> findAllByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(
            ReminderStatus status,
            LocalDateTime remindAt,
            Pageable pageable
    );
}
