package com.dww.chat_app.repository;

import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByIdAndDeletedAtIsNull(UUID id);

    List<Task> findAllByProjectIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID projectId);

    List<Task> findAllBySectionIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID sectionId);

    List<Task> findAllByParentTaskIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID parentTaskId);

    List<Task> findAllByAssigneeIdAndStatusAndDeletedAtIsNullOrderByDueAtAsc(
            UUID assigneeId,
            TaskStatus status
    );
}
