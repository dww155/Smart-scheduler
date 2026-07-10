package com.dww.chat_app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(nullable = false)
    @CreationTimestamp
    LocalDateTime sentAt;

    LocalDateTime recalledAt;

    @UpdateTimestamp
    LocalDateTime editedAt;

    @PrePersist
    public void persist() {
        recalledAt = null;
        editedAt = null;
    }
}
