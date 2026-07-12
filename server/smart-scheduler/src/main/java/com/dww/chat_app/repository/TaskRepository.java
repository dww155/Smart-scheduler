package com.dww.chat_app.repository;

import com.dww.chat_app.entity.Task;
import com.dww.chat_app.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByIdAndDeletedAtIsNull(UUID id);

    List<Task> findAllByProjectIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID projectId);

    List<Task> findAllBySectionIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID sectionId);

    List<Task> findAllByParentTaskIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID parentTaskId);

    List<Task> findAllByLabelsIdAndDeletedAtIsNull(UUID labelId);

    List<Task> findAllByAssigneeIdAndProjectWorkspaceIdAndDeletedAtIsNull(
            UUID assigneeId,
            UUID workspaceId
    );

    List<Task> findAllByAssigneeIdAndStatusAndDeletedAtIsNullOrderByDueAtAsc(
            UUID assigneeId,
            TaskStatus status
    );

    @Query("""
            select distinct task
            from Task task
            left join fetch task.labels
            where task.deletedAt is null
              and task.archivedAt is null
              and task.project.deletedAt is null
              and task.project.archivedAt is null
              and task.project.workspace.deletedAt is null
              and task.project.workspace.id in :workspaceIds
            order by task.dueAt asc, task.sortOrder asc
            """)
    List<Task> findAllVisibleByWorkspaceIds(@Param("workspaceIds") Collection<UUID> workspaceIds);
}
