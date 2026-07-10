package com.dww.chat_app.dto.message;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageSendRequest {

    @NotNull(message = "khong duoc de trong")
    private String content;

}
