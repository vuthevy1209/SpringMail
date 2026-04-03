package com.vuthevy1209.springmail;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GmailService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GmailService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public List<EmailResponse> getRecentEmails(OAuth2AuthenticationToken authentication) throws IOException {
        // 1. Lấy Access Token từ Spring Security sau khi user đã login
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());

        String accessToken = client.getAccessToken().getTokenValue();

        // 2. Khởi tạo Gmail Service của Google SDK
        Gmail service = new Gmail.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
                .setApplicationName("SpringMail")
                .build();

        // 3. Gọi API lấy danh sách 10 mail gần nhất
        // ListMessagesResponse response = service.users().messages().list("me")
        //         .setMaxResults(10L)
        //         .execute();

        // Chỉnh sửa: Chỉ lấy 10 email trong mục thư CHÍNH (Primary)
        ListMessagesResponse response = service.users().messages().list("me")
                .setQ("category:primary") // Lọc theo danh mục chính
                .setMaxResults(10L)
                .execute();

        List<EmailResponse> emails = new ArrayList<>();
        if (response.getMessages() != null) {
            for (Message msg : response.getMessages()) {
                // Lấy chi tiết từng mail để đọc snippet và header
                Message fullMsg = service.users().messages().get("me", msg.getId()).execute();

                String from = "";
                String subject = "";
                if (fullMsg.getPayload() != null && fullMsg.getPayload().getHeaders() != null) {
                    for (MessagePartHeader header : fullMsg.getPayload().getHeaders()) {
                        if ("From".equalsIgnoreCase(header.getName())) {
                            from = header.getValue();
                        } else if ("Subject".equalsIgnoreCase(header.getName())) {
                            subject = header.getValue();
                        }
                    }
                }

                emails.add(new EmailResponse(from, subject, fullMsg.getSnippet()));
            }
        }
        return emails;
    }
}
