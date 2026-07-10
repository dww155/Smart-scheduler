package com.dww.chat_app.dto.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskAttachmentCreationRequest {

    @NotNull(message = "INVALID_REQUEST")
    MultipartFile file;

    @JsonIgnore
    @AssertTrue(message = "INVALID_REQUEST")
    public boolean isFilePresent() {
        return file != null && !file.isEmpty();
    }
}
