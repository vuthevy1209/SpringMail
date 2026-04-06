package com.vuthevy1209.springmail.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.vuthevy1209.springmail.dto.response.EmailResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GmailService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GmailService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public List<EmailResponse> getRecentEmails(OAuth2AuthenticationToken authentication, String category) throws IOException {
        // 1. Lấy Access Token từ Spring Security
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());

        String accessToken = client.getAccessToken().getTokenValue();

        // 2. Khởi tạo Gmail Service
        Gmail service = new Gmail.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
                .setApplicationName("SpringMail")
                .build();

        // 3. Lấy danh sách ID tin nhắn
        ListMessagesResponse response = service.users().messages().list("me")
                .setQ("category:" + category)
                .setMaxResults(10L)
                .execute();

        List<EmailResponse> emails = new ArrayList<>();
        if (response.getMessages() != null) {
            for (Message msg : response.getMessages()) {
                // Lấy chi tiết toàn bộ message
                Message fullMsg = service.users().messages().get("me", msg.getId()).execute();
                
                String from = "";
                String subject = "";
                String date = "";
                
                // Trích xuất Headers
                if (fullMsg.getPayload().getHeaders() != null) {
                    for (MessagePartHeader header : fullMsg.getPayload().getHeaders()) {
                        switch (header.getName()) {
                            case "From" -> from = header.getValue();
                            case "Subject" -> subject = header.getValue();
                            case "Date" -> date = header.getValue();
                        }
                    }
                }

                String senderName = extractSenderName(from);
                
                // Trích xuất trạng thái Chưa đọc (Unread)
                boolean unread = fullMsg.getLabelIds() != null && fullMsg.getLabelIds().contains("UNREAD");
                
                // Trích xuất file đính kèm
                List<String> attachments = new ArrayList<>();
                if (fullMsg.getPayload() != null) {
                    extractAttachments(fullMsg.getPayload(), attachments);
                }

                // Trích xuất Content (Body)
                String content = getMessageBody(fullMsg.getPayload());

                emails.add(new EmailResponse(fullMsg.getId(), from, senderName, subject, date, fullMsg.getSnippet(), content, unread, attachments));
            }
        }
        return emails;
    }

    /**
     * Phương thức xử lý lấy nội dung Email (Xử lý được cả Multipart)
     */
    private String getMessageBody(MessagePart part) {
        // Ưu tiên lấy HTML trước
        String htmlContent = extractMimeTypeString(part, "text/html");
        if (htmlContent != null && !htmlContent.isEmpty()) {
            return htmlContent;
        }

        // Nếu không có HTML thì lấy Plain Text
        String plainContent = extractMimeTypeString(part, "text/plain");
        if (plainContent != null && !plainContent.isEmpty()) {
            return plainContent;
        }
        
        return "";
    }

    private String extractMimeTypeString(MessagePart part, String mimeType) {
        if (part.getMimeType().contains(mimeType) && part.getBody().getData() != null) {
            return decodeBase64(part.getBody().getData());
        }

        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                String result = extractMimeTypeString(subPart, mimeType);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }
        return null;
    }

    private String decodeBase64(String base64Data) {
        return new String(Base64.getUrlDecoder().decode(base64Data));
    }

    private String extractSenderName(String from) {
        if (from == null) return "";
        if (from.contains("<")) {
            return from.substring(0, from.indexOf("<")).replace("\"", "").trim();
        }
        return from;
    }

    private void extractAttachments(MessagePart part, List<String> attachments) {
        if (part.getFilename() != null && !part.getFilename().isEmpty()) {
            attachments.add(part.getFilename());
        }
        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                extractAttachments(subPart, attachments);
            }
        }
    }
}