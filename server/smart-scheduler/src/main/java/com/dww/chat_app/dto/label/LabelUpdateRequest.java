package com.dww.chat_app.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LabelUpdateRequest {

    @NotBlank(message = "INVALID_REQUEST")
    @Size(max = 100, message = "INVALID_REQUEST")
    String name;

    @Size(max = 20, message = "INVALID_REQUEST")
    String color;
}
