package com.dww.chat_app.dto.message;

import com.dww.chat_app.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageSendResponse {
    UUID id;

    User sender;

    String content;

    LocalDateTime sentAt;

    LocalDateTime recalledAt;

    LocalDateTime editedAt;

}
