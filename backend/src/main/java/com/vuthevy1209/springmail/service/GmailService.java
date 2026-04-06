package com.vuthevy1209.springmail.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.vuthevy1209.springmail.dto.response.EmailResponse;
import com.vuthevy1209.springmail.dto.response.ThreadResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GmailService {

    public GmailService() {
    }

    public List<ThreadResponse> getRecentEmails(OAuth2AuthorizedClient client, String folder, String category) throws IOException {

        String accessToken = client.getAccessToken().getTokenValue();

        // 2. Khởi tạo Gmail Service
        Gmail service = new Gmail.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
                .setApplicationName("SpringMail")
                .build();

        // 3. Xây dựng query phù hợp với từng loại folder & category
        String query = buildQuery(folder, category);

        // 4. Lấy danh sách ID thread
        com.google.api.services.gmail.model.ListThreadsResponse response = service.users().threads().list("me")
                .setQ(query)
                .setMaxResults(15L)
                .execute();

        List<ThreadResponse> threadResponses = new ArrayList<>();
        if (response.getThreads() != null) {
            for (com.google.api.services.gmail.model.Thread threadSnippet : response.getThreads()) {
                // Lấy chi tiết toàn bộ thread bao gồm các messages
                com.google.api.services.gmail.model.Thread fullThread = service.users().threads().get("me", threadSnippet.getId()).execute();

                List<EmailResponse> messages = new ArrayList<>();
                boolean threadUnread = false;
                String threadSubject = "";
                String latestDate = "";
                String latestSenderName = "";
                Long threadInternalDate = 0L;

                if (fullThread.getMessages() != null) {
                    List<Message> threadMessages = new ArrayList<>(fullThread.getMessages());
                    // Đảm bảo tin nhắn được sắp xếp chuẩn theo thời gian (cũ -> mới)
                    threadMessages.sort((m1, m2) -> Long.compare(m1.getInternalDate(), m2.getInternalDate()));

                    for (Message fullMsg : threadMessages) {
                        String from = "";
                        String to = "";
                        String subject = "";
                        String date = "";
                        Long internalDate = fullMsg.getInternalDate();

                        // Trích xuất Headers cho message hiện tại
                        if (fullMsg.getPayload().getHeaders() != null) {
                            for (MessagePartHeader header : fullMsg.getPayload().getHeaders()) {
                                switch (header.getName()) {
                                    case "From"    -> from    = header.getValue();
                                    case "To"      -> to      = header.getValue();
                                    case "Subject" -> subject = header.getValue();
                                    case "Date"    -> date    = header.getValue();
                                }
                            }
                        }

                        String senderName = extractSenderName(from);
                        String senderEmail = extractSenderEmail(from);

                        // Trích xuất trạng thái Chưa đọc (Unread)
                        boolean unread = fullMsg.getLabelIds() != null && fullMsg.getLabelIds().contains("UNREAD");
                        if (unread) threadUnread = true;

                        // Trích xuất file đính kèm
                        List<String> attachments = new ArrayList<>();
                        if (fullMsg.getPayload() != null) {
                            extractAttachments(fullMsg.getPayload(), attachments);
                        }

                        // Trích xuất Content (Body)
                        String content = getMessageBody(fullMsg.getPayload());

                        messages.add(new EmailResponse(
                                fullMsg.getId(), from, to, senderName, senderEmail, subject, date,
                                fullMsg.getSnippet(), content, unread, internalDate, attachments
                        ));
                    }

                    if (!messages.isEmpty()) {
                        EmailResponse firstMsg = messages.get(0);
                        EmailResponse lastMsg = messages.get(messages.size() - 1); // newest message

                        threadSubject = firstMsg.subject() != null && !firstMsg.subject().isEmpty() ? firstMsg.subject() : "(No Subject)";
                        latestDate = lastMsg.date();
                        latestSenderName = lastMsg.senderName();
                        threadInternalDate = lastMsg.internalDate();
                    }
                }

                threadResponses.add(new ThreadResponse(
                        fullThread.getId(),
                        threadSubject,
                        fullThread.getSnippet(), // Snippet tóm tắt từ Google cho toàn thread
                        latestDate,
                        latestSenderName,
                        threadUnread,
                        messages.size(),
                        threadInternalDate,
                        messages
                ));
            }
        }
        
        // Sắp xếp các chuỗi hội thoại: Chuỗi nào có tin nhắn mới nhất sẽ được đưa lên đầu (Mới nhất -> Cũ nhất)
        threadResponses.sort((t1, t2) -> Long.compare(t2.internalDate(), t1.internalDate()));
        
        return threadResponses;
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

    private String extractSenderEmail(String from) {
        if (from == null) return "";
        if (from.contains("<") && from.contains(">")) {
            return from.substring(from.indexOf("<") + 1, from.indexOf(">")).trim();
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