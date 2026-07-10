package com.dww.chat_app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "task_sections",
        indexes = {
                @Index(name = "idx_task_sections_project_id", columnList = "project_id"),
                @Index(name = "idx_task_sections_project_sort_order", columnList = "project_id, sort_order"),
                @Index(name = "idx_task_sections_archived_at", columnList = "archived_at")
        }
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Column(name = "name", nullable = false, length = 150)
    String name;

    @Column(name = "description", length = 500)
    String description;

    @Column(name = "sort_order", nullable = false)
    int sortOrder;

    @Column(name = "archived_at")
    LocalDateTime archivedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    Long version;

}
