package com.dww.chat_app.entity;

import com.dww.chat_app.entity.enums.ProjectViewType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "projects",
        indexes = {
                @Index(name = "idx_projects_workspace_id", columnList = "workspace_id"),
                @Index(name = "idx_projects_created_by_id", columnList = "created_by_id"),
                @Index(name = "idx_projects_workspace_archived", columnList = "workspace_id, archived_at"),
                @Index(name = "idx_projects_workspace_sort_order", columnList = "workspace_id, sort_order"),
                @Index(name = "idx_projects_deleted_at", columnList = "deleted_at")
        }
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    User createdBy;

    @Column(name = "name", nullable = false, length = 150)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "color", length = 20)
    String color;

    @Column(name = "icon", length = 50)
    String icon;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "view_type", nullable = false, length = 20)
    ProjectViewType viewType = ProjectViewType.LIST;

    @Column(name = "sort_order", nullable = false)
    int sortOrder;

    @Column(name = "archived_at")
    LocalDateTime archivedAt;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

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
        if (viewType == null) {
            viewType = ProjectViewType.LIST;
        }
    }
}
