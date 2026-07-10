package com.dww.chat_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    @Builder.Default
    Instant timestamp = Instant.now();

    @Builder.Default
    boolean success = true;

    @Builder.Default
    int code = 200;

    @Builder.Default
    String message = "Success";

    T result;

    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message("Success")
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T result) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message(message)
                .result(result)
                .build();
    }

    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Success")
                .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> error(int code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
}
