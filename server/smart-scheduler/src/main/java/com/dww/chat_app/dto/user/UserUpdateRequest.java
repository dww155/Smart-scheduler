package com.dww.chat_app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @NotBlank(message = "INVALID_USERNAME")
    @Size(min = 3, max = 50, message = "INVALID_USERNAME")
    String username;
}
