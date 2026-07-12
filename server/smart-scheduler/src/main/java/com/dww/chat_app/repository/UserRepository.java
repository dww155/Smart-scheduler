package com.dww.chat_app.repository;

import com.dww.chat_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndActiveTrueAndDeletedAtIsNull(String username);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    List<User> findAllByDeletedAtIsNull();
}
