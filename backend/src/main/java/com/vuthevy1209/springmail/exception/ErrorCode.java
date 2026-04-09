package com.vuthevy1209.springmail.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    SUCCESS("success", "Success", HttpStatus.OK),
    UNAUTHENTICATED("unauthenticated", "Unauthenticated", HttpStatus.UNAUTHORIZED),
    GMAIL_UNAUTHENTICATED("gmail-unauthenticated", "Gmail authentication failed", HttpStatus.UNAUTHORIZED),
    GMAIL_API_ERROR("gmail-api-error", "Error calling Gmail API", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_EXCEPTION("uncategorized-exception", "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    ErrorCode(String code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    String code;
    String message;
    HttpStatusCode statusCode;
}
