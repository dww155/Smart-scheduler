package com.dww.chat_app.repository;

import com.dww.chat_app.entity.TaskSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskSectionRepository extends JpaRepository<TaskSection, UUID> {
    List<TaskSection> findAllByProjectIdAndArchivedAtIsNullOrderBySortOrderAsc(UUID projectId);
}
