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
    INVALID_EMAIL(1003, "Email is invalid", HttpStatus.BAD_REQUEST),
    INVALID_STATUS(1004, "Status is invalid", HttpStatus.BAD_REQUEST),
    WEAK_PASSWORD(1005, "Password must contain uppercase, lowercase, number, and special character", HttpStatus.BAD_REQUEST),
    ROLE_REQUIRED(1006, "At least one role is required", HttpStatus.BAD_REQUEST),

    USER_NOT_FOUND(1100, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1101, "User already exists", HttpStatus.CONFLICT),

    UNAUTHENTICATED(1200, "Wrong username or password", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1201, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1202, "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1203, "Token expired", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1204, "You do not have permission to perform this action", HttpStatus.FORBIDDEN),

    NOT_FOUND(1301, "Not found", HttpStatus.NOT_FOUND),
    WORKSPACE_NOT_FOUND(1302, "Workspace not found", HttpStatus.NOT_FOUND),
    WORKSPACE_MEMBER_NOT_FOUND(1303, "Workspace member not found", HttpStatus.NOT_FOUND),

    RESOURCE_CONFLICT(1400, "The requested change conflicts with existing data", HttpStatus.CONFLICT),
    DATA_INTEGRITY_VIOLATION(1401, "The requested change violates data constraints", HttpStatus.CONFLICT),
    OPTIMISTIC_LOCK_CONFLICT(1402, "The resource was changed by another request; refresh and try again", HttpStatus.CONFLICT);
    int code;
    String message;
    HttpStatus status;
}
