package com.dww.chat_app.entity;

import com.dww.chat_app.entity.enums.ReminderChannel;
import com.dww.chat_app.entity.enums.ReminderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "task_reminders",
        indexes = {
                @Index(
                        name = "idx_task_reminders_status_remind_at",
                        columnList = "status, remind_at"
                ),
                @Index(
                        name = "idx_task_reminders_recipient_status_remind_at",
                        columnList = "recipient_id, status, remind_at"
                ),
                @Index(
                        name = "idx_task_reminders_task_recipient",
                        columnList = "task_id, recipient_id"
                )
        }
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    User recipient;

    @Column(name = "remind_at", nullable = false)
    LocalDateTime remindAt;

    @Column(name = "time_zone", length = 100)
    String timeZone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    ReminderChannel channel = ReminderChannel.IN_APP;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "sent_at")
    LocalDateTime sentAt;

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
    public void applyDefaults() {
        if (channel == null) {
            channel = ReminderChannel.IN_APP;
        }
        if (status == null) {
            status = ReminderStatus.PENDING;
        }
    }
}
