package com.dww.chat_app.dto.user;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "INVALID_USERNAME")
    @Size(min = 3, max = 50, message = "INVALID_USERNAME")
    private String username;

    @NotBlank(message = "INVALID_EMAIL")
    @Email(message = "INVALID_EMAIL")
    @Size(max = 255, message = "INVALID_EMAIL")
    String email;

    @NotBlank(message = "INVALID_PASSWORD")
    @Size(min = 8, max = 100, message = "INVALID_PASSWORD")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$",
            message = "WEAK_PASSWORD"
    )
    private String password;

    @NotEmpty(message = "ROLE_REQUIRED")
    List<String> roleNames;
}
