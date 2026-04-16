package com.vuthevy1209.springmail.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vuthevy1209.springmail.service.mail.MailSyncService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class GmailWebhookController {

    private final MailSyncService mailSyncService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    public static class PubSubRequest {
        private PubSubMessage message;
        private String subscription;
    }

    @Data
    public static class PubSubMessage {
        private String data;
        private String messageId;
        private String publishTime;
    }

    @Data
    public static class GmailNotification {
        private String emailAddress;
        private String historyId;
    }

    @PostMapping("/gmail")
    public ResponseEntity<Void> handleGmailNotification(
            @RequestBody PubSubRequest request) {

        try {
            if (request.getMessage() == null || request.getMessage().getData() == null) {
                log.warn("Received Pub/Sub message with missing data");
                return ResponseEntity.badRequest().build();
            }

            // Decode base64 message từ Pub/Sub (Google gửi base64 hoặc base64Url)
            byte[] decodedBytes = Base64.getUrlDecoder().decode(request.getMessage().getData());
            String decodedData = new String(decodedBytes);
   
            // Parse JSON để lấy emailAddress và historyId
            GmailNotification notification = objectMapper.readValue(
                    decodedData, GmailNotification.class
            );

            String email = notification.getEmailAddress();
            String historyId = notification.getHistoryId();

            if (email != null && historyId != null) {
                log.info("Received Gmail notification for email: {}, historyId: {}", email, historyId);
                mailSyncService.processNewEmails(email, historyId);
            } else {
                log.warn("Received Gmail notification with missing email or historyId: {}", decodedData);
            }

            // Google Pub/Sub yêu cầu response 200 để xác nhận đã nhận được message, 
            // nếu trả về lỗi (4xx hoặc 5xx) thì sẽ tự động retry gửi lại sau một khoảng thời gian.
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing Gmail webhook notification", e);
            // Có thể trả về 500 để Google Pub/Sub gửi lại (retry) nếu lỗi, 
            // hoặc 200 nếu bỏ qua/lỗi parsing để tránh kẹt queue mãi
            return ResponseEntity.ok().build();
        }
    }
}
