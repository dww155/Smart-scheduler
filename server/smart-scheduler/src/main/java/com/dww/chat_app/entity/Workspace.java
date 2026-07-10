package com.dww.chat_app.entity;

import com.dww.chat_app.entity.enums.WorkspaceType;
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
        name = "workspaces",
        indexes = {
                @Index(name = "idx_workspaces_owner_id", columnList = "owner_id"),
                @Index(name = "idx_workspaces_type", columnList = "type"),
                @Index(name = "idx_workspaces_archived_at", columnList = "archived_at"),
                @Index(name = "idx_workspaces_deleted_at", columnList = "deleted_at")
        }
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;

    @Builder.Default
    @OneToMany(mappedBy = "workspace", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    Set<WorkspaceMember> members = new HashSet<>();

    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    WorkspaceType type = WorkspaceType.PERSONAL;

    @Column(name = "color", length = 20)
    String color;

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
        if (type == null) {
            type = WorkspaceType.PERSONAL;
        }
    }

    public void addMember(WorkspaceMember member) {
        members.add(member);
        member.setWorkspace(this);
    }
}
