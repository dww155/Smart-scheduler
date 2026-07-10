package com.dww.chat_app.entity;

import com.dww.chat_app.entity.enums.RecurrenceMode;
import com.dww.chat_app.entity.enums.TaskPriority;
import com.dww.chat_app.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(
                        name = "idx_tasks_project_status_due",
                        columnList = "project_id, status, due_at"
                ),
                @Index(
                        name = "idx_tasks_assignee_status_due",
                        columnList = "assignee_id, status, due_at"
                ),
                @Index(
                        name = "idx_tasks_section_order",
                        columnList = "section_id, sort_order"
                ),
                @Index(
                        name = "idx_tasks_parent_order",
                        columnList = "parent_task_id, sort_order"
                )
        }
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    TaskSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    Task parentTask;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    User assignee;

    @Column(name = "title", nullable = false, length = 500)
    String title;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    TaskPriority priority = TaskPriority.NONE;

    @Column(name = "start_at")
    LocalDateTime startAt;

    @Column(name = "due_at")
    LocalDateTime dueAt;

    @Builder.Default
    @Column(name = "all_day", nullable = false)
    boolean allDay = true;

    @Column(name = "recurrence_rule", length = 1000)
    String recurrenceRule;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_mode", nullable = false, length = 30)
    RecurrenceMode recurrenceMode = RecurrenceMode.NONE;

    @Column(name = "time_zone", length = 100)
    String timeZone;

    @Column(name = "completed_at")
    LocalDateTime completedAt;

    @Column(name = "archived_at")
    LocalDateTime archivedAt;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    @Column(name = "sort_order", nullable = false)
    int sortOrder;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_labels",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id"),
            indexes = @Index(
                    name = "idx_task_labels_label_task",
                    columnList = "label_id, task_id"
            )
    )
    Set<Label> labels = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    Long version;

    @PrePersist
    public void persist() {
        if (status == null) {
            status = TaskStatus.TODO;
        }
        if (priority == null) {
            priority = TaskPriority.NONE;
        }
        if (recurrenceMode == null
                || recurrenceMode == RecurrenceMode.NONE && recurrenceRule != null) {
            recurrenceMode = recurrenceRule == null
                    ? RecurrenceMode.NONE
                    : RecurrenceMode.FIXED_SCHEDULE;
        }
    }
}
