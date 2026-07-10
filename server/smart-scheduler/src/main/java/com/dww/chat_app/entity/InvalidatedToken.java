package com.dww.chat_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invalidated_token")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvalidatedToken {
    @Id
    @Column(name = "jwtId")
    UUID jwtId;

    @Column(name = "logout_time")
    LocalDateTime time;
}
