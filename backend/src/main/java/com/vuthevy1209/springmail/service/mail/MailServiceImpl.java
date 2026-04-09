package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.service.gmail.GmailMapper;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentBodyDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final GmailService gmailService;


    @Override
    public List<MailThreadResponse> getRecentEmails(String folder, String category) throws IOException {

        // 1. Lấy Access Token từ SecurityUtils
        String accessToken = SecurityUtils.getAccessToken("google");
        if (accessToken == null) {
            throw new IOException("Failed to authorize OAuth2 client or get access token");
        }

        System.out.println("Access Token: " + accessToken); // Debug: In ra access token để kiểm tra

        // 2. Xây dựng query phù hợp với từng loại folder & category
        String query = buildQuery(folder, category);

        // 3. Lấy danh sách ID thread qua GmailClient
        GmailListThreadsResponseDto response = gmailService.listThreads(accessToken, query, 20L, null);

        List<MailThreadResponse> threadResponses = new ArrayList<>();
        if (response.getThreads() != null) {
            for (GmailThreadDto threadSnippet : response.getThreads()) {
                // CHỈ lấy metadata (headers) qua GmailClient
                GmailThreadDto fullThread = gmailService.getThread(accessToken, threadSnippet.getId(), "metadata", List.of("Subject", "Date", "From"));
                
                // Sử dụng GmailMapper để làm giàu dữ liệu (bóc tách headers)
                if (fullThread.getMessages() != null) {
                    fullThread.getMessages().forEach(GmailMapper::enrichGmailMessageDto);
                }

                // Map sang MailThreadResponse (messages list rỗng cho danh sách recent)
                MailThreadResponse threadResponse = GmailMapper.toMailThreadResponse(fullThread);
                if (threadResponse != null) {
                    // Xoá danh sách tin nhắn để tiết kiệm băng thông khi lấy danh sách thread
                    threadResponses.add(new MailThreadResponse(
                            threadResponse.id(),
                            threadResponse.subject(),
                            threadResponse.snippet(),
                            threadResponse.latestDate(),
                            threadResponse.latestSenderName(),
                            threadResponse.unread(),
                            threadResponse.messageCount(),
                            threadResponse.internalDate(),
                            new ArrayList<>()
                    ));
                }
            }
        }
        
        // Sắp xếp các chuỗi hội thoại: Chuỗi nào có tin nhắn mới nhất sẽ được đưa lên đầu (Mới nhất -> Cũ nhất)
        threadResponses.sort((t1, t2) -> Long.compare(t2.internalDate(), t1.internalDate()));
        
        return threadResponses;
    }

    @Override
    public MailThreadResponse getThreadDetails(String threadId) throws IOException {
        String accessToken = SecurityUtils.getAccessToken("google");
        if (accessToken == null) {
            throw new IOException("Failed to authorize OAuth2 client or get access token");
        }

        // Lấy FULL chi tiết thread bao gồm cả body qua GmailClient
        GmailThreadDto fullThread = gmailService.getThread(accessToken, threadId, "full", null);

        if (fullThread.getMessages() != null) {
            // Đảm bảo tin nhắn được sắp xếp chuẩn theo thời gian (cũ -> mới)
            fullThread.getMessages().sort((m1, m2) -> Long.compare(m1.getInternalDate(), m2.getInternalDate()));
            // Làm giàu dữ liệu cho từng tin nhắn (bóc tách body, headers, attachments)
            fullThread.getMessages().forEach(GmailMapper::enrichGmailMessageDto);
        }

        return GmailMapper.toMailThreadResponse(fullThread);
    }

    /**
     * Xây dựng Gmail query string phù hợp với folder và category được yêu cầu.
     */
    private String buildQuery(String folder, String category) {
        String base = switch (folder.toLowerCase()) {
            case "sent"   -> "in:sent";
            case "drafts" -> "in:drafts";
            case "trash"  -> "in:trash";
            default       -> "in:inbox";
        };

        if (category != null && !category.isEmpty()) {
            return base + " category:" + category.toLowerCase();
        }

        return base;
    }

    @Override
    public byte[] getAttachment(String messageId, String attachmentId) throws IOException {
        String accessToken = SecurityUtils.getAccessToken("google");
        if (accessToken == null) {
            throw new IOException("Failed to authorize OAuth2 client or get access token");
        }

        GmailAttachmentBodyDto attachment = gmailService.getAttachment(accessToken, messageId, attachmentId);

        return Base64.getUrlDecoder().decode(attachment.getData());
    }
}
