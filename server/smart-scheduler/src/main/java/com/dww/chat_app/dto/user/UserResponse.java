package com.dww.chat_app.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    UUID id;

    String username;

    String email;

    boolean active;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
