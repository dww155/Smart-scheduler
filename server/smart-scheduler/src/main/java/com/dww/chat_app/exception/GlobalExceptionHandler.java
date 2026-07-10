package com.dww.chat_app.exception;

import com.dww.chat_app.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());

        HttpStatus status = errorCode.getStatus();

        return ResponseEntity.status(status).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String errorKey = exception.getFieldErrors().isEmpty()
                ? ErrorCode.INVALID_REQUEST.name()
                : exception.getFieldErrors().getFirst().getDefaultMessage();

        ErrorCode errorCode = getErrorCode(errorKey);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatus()).body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUncategorizedException(Exception exception) {
        log.error("Uncategorized exception", exception);

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatus()).body(apiResponse);
    }

    private ErrorCode getErrorCode(String errorKey) {
        try {
            return ErrorCode.valueOf(errorKey);
        } catch (IllegalArgumentException exception) {
            return ErrorCode.INVALID_REQUEST;
        }
    }
}
