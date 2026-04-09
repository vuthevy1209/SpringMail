package com.vuthevy1209.springmail.service.gmail.feign;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.vuthevy1209.springmail.exception.AppException;
import com.vuthevy1209.springmail.exception.ErrorCode;
import com.vuthevy1209.springmail.service.gmail.dto.error.GoogleErrorDto;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class GmailFeignConfiguration {

    @Bean
    public Decoder feignDecoder() {
        return new GmailFeignDecoder();
    }

    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new GmailFeignErrorDecoder();
    }

    public static class GmailFeignDecoder implements Decoder {
        private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        @Override
        public Object decode(Response response, Type type) throws IOException {
            if (response.body() == null) {
                return null;
            }
            try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
                return jsonFactory.fromReader(reader, (Class<?>) type);
            }
        }
    }

    @Slf4j
    public static class GmailFeignErrorDecoder implements ErrorDecoder {
        private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            String body = "empty body";
            if (response.body() != null) {
                try {
                    body = feign.Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.error("Error reading response body: {}", e.getMessage());
                }
            }

            log.error("Gmail API Error [{}]: Status={}, Reason={}, Body={}",
                    methodKey, response.status(), response.reason(), body);

            String googleMessage = null;
            if (!"empty body".equals(body)) {
                try {
                    GoogleErrorDto errorResponse = jsonFactory.fromString(body, GoogleErrorDto.class);
                    if (errorResponse != null && errorResponse.getError() != null) {
                        GoogleErrorDto.ErrorDetail detail = errorResponse.getError();
                        googleMessage = detail.getMessage();

                        log.error("--- GMAIL API ERROR ---");
                        log.error("Status: {}", detail.getStatus());
                        log.error("Code: {}", detail.getCode());
                        log.error("Message: {}", detail.getMessage());

                        if (detail.getErrors() != null && !detail.getErrors().isEmpty()) {
                            detail.getErrors().forEach(err ->
                                    log.error("Detail: [Reason: {}, Domain: {}, Message: {}]",
                                            err.getReason(), err.getDomain(), err.getMessage())
                            );
                        }
                        log.error("-----------------------");
                    }
                } catch (Exception e) {
                    log.warn("Could not parse body as GoogleErrorDto: {}", e.getMessage());
                }
            }

            // Handle specific status codes
            if (response.status() == 401 || response.status() == 403) {
                return googleMessage != null
                        ? new AppException(ErrorCode.GMAIL_UNAUTHENTICATED, googleMessage)
                        : new AppException(ErrorCode.GMAIL_UNAUTHENTICATED);
            }

            return googleMessage != null
                    ? new AppException(ErrorCode.GMAIL_API_ERROR, googleMessage)
                    : new AppException(ErrorCode.GMAIL_API_ERROR);
        }
    }
}
