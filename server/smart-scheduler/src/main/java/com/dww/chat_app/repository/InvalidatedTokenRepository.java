package com.dww.chat_app.repository;

import com.dww.chat_app.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, UUID> {
    long deleteAllByTimeBefore(LocalDateTime cutoff);
}
