package com.dww.chat_app.dto.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateStatusRequest {
    Boolean active;

    List<String> roleNames;
}
