package com.dww.chat_app.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_REQUEST(1000, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1001, "Username must be between 3 and 50 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1002, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    WEAK_PASSWORD(1002, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1003, "Email is invalid", HttpStatus.BAD_REQUEST),
    INVALID_STATUS(1004, "Status is invalid", HttpStatus.BAD_REQUEST),

    USER_NOT_FOUND(1100, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1101, "User already exists", HttpStatus.CONFLICT),

    UNAUTHENTICATED(1200, "Wrong username or password", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1201, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1202, "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1203, "Token expired", HttpStatus.UNAUTHORIZED),

    NOT_FOUND(1301, "Not found", HttpStatus.NOT_FOUND);
    int code;
    String message;
    HttpStatus status;
}
