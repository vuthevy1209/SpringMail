package com.vuthevy1209.springmail.exception;

import com.vuthevy1209.springmail.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    // Handle all uncategorized exceptions
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<?>> handlingRuntimeException(Exception exception, HttpServletRequest request) {
        log.error("Exception [{} {}]: ", request.getMethod(), request.getRequestURI(), exception);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(exception.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(apiResponse);
    }

    // Handle custom application exceptions
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception, HttpServletRequest request) {
        log.error("AppException [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // Handle validation exceptions
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handlingValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.error("ValidationException [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code("validation-error")
                .message(message)
                .build();

        return ResponseEntity.badRequest().body(apiResponse);
    }
}
