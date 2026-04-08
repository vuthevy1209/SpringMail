package com.vuthevy1209.springmail.service.mail;

import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import com.vuthevy1209.springmail.dto.response.mail.MailAttachmentResponse;
import com.vuthevy1209.springmail.dto.response.mail.MailResponse;
import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.service.gmail.GmailClient;
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

    private final GmailClient gmailClient;


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
        ListThreadsResponse response = gmailClient.listThreads(accessToken, query, 20L);

        List<MailThreadResponse> threadResponses = new ArrayList<>();
        if (response.getThreads() != null) {
            for (Thread threadSnippet : response.getThreads()) {
                // CHỈ lấy metadata (headers) qua GmailClient
                Thread fullThread = gmailClient.getThread(accessToken, threadSnippet.getId(), "metadata", List.of("Subject", "Date", "From"));

                String threadSubject = "";
                String latestDate = "";
                String latestSenderName = "";
                Long threadInternalDate = 0L;
                boolean threadUnread = false;
                int messageCount = 0;

                if (fullThread.getMessages() != null) {
                    messageCount = fullThread.getMessages().size();
                    // Lấy tin nhắn cuối cùng (thường là cái mới nhất trong thread)
                    Message lastMsgSnippet = fullThread.getMessages().get(messageCount - 1);
                    // Và tin nhắn đầu tiên để lấy Subect
                    Message firstMsgSnippet = fullThread.getMessages().get(0);

                    threadInternalDate = lastMsgSnippet.getInternalDate();

                    // Trích xuất Subject từ tin nhắn đầu tiên
                    if (firstMsgSnippet.getPayload().getHeaders() != null) {
                        for (MessagePartHeader header : firstMsgSnippet.getPayload().getHeaders()) {
                            if (header.getName().equalsIgnoreCase("Subject")) {
                                threadSubject = header.getValue();
                                break;
                            }
                        }
                    }

                    // Trích xuất Date và From từ tin nhắn cuối cùng
                    if (lastMsgSnippet.getPayload().getHeaders() != null) {
                        for (MessagePartHeader header : lastMsgSnippet.getPayload().getHeaders()) {
                            if (header.getName().equalsIgnoreCase("Date")) {
                                latestDate = header.getValue();
                            } else if (header.getName().equalsIgnoreCase("From")) {
                                latestSenderName = extractSenderName(header.getValue());
                            }
                        }
                    }

                    // Kiểm tra xem có tin nhắn nào chưa đọc không
                    for (Message m : fullThread.getMessages()) {
                        if (m.getLabelIds() != null && m.getLabelIds().contains("UNREAD")) {
                            threadUnread = true;
                            break;
                        }
                    }
                }

                threadResponses.add(new MailThreadResponse(
                        fullThread.getId(),
                        (threadSubject == null || threadSubject.isEmpty()) ? "(No Subject)" : threadSubject,
                        fullThread.getSnippet(),
                        latestDate,
                        latestSenderName,
                        threadUnread,
                        messageCount,
                        threadInternalDate,
                        new ArrayList<>() // Không trả về nội dung tin nhắn ở danh sách
                ));
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
        Thread fullThread = gmailClient.getThread(accessToken, threadId, "full", null);

        List<MailResponse> messages = new ArrayList<>();
        String threadSubject = "";
        String latestDate = "";
        String latestSenderName = "";
        Long threadInternalDate = 0L;
        boolean threadUnread = false;

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
                boolean unread = fullMsg.getLabelIds() != null && fullMsg.getLabelIds().contains("UNREAD");
                if (unread) threadUnread = true;

                List<MailAttachmentResponse> attachments = new ArrayList<>();
                if (fullMsg.getPayload() != null) {
                    extractAttachments(fullMsg.getPayload(), attachments);
                }

                String content = getMessageBody(fullMsg.getPayload());

                messages.add(new MailResponse(
                        fullMsg.getId(), from, to, senderName, senderEmail, subject, date,
                        fullMsg.getSnippet(), content, unread, internalDate, attachments
                ));
            }

            if (!messages.isEmpty()) {
                MailResponse firstMsg = messages.get(0);
                MailResponse lastMsg = messages.get(messages.size() - 1);
                threadSubject = (firstMsg.subject() != null && !firstMsg.subject().isEmpty()) ? firstMsg.subject() : "(No Subject)";
                latestDate = lastMsg.date();
                latestSenderName = lastMsg.senderName();
                threadInternalDate = lastMsg.internalDate();
            }
        }

        return new MailThreadResponse(
                fullThread.getId(),
                threadSubject,
                fullThread.getSnippet(),
                latestDate,
                latestSenderName,
                threadUnread,
                messages.size(),
                threadInternalDate,
                messages
        );
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

    private void extractAttachments(MessagePart part, List<MailAttachmentResponse> attachments) {
        if (part.getFilename() != null && !part.getFilename().isEmpty()) {
            String contentId = null;
            if (part.getHeaders() != null) {
                for (MessagePartHeader header : part.getHeaders()) {
                    if (header.getName().equalsIgnoreCase("Content-ID")) {
                        // Content-ID thường có dạng <id>, ta cần bỏ dấu <>
                        contentId = header.getValue().replaceAll("[<>]", "");
                        break;
                    }
                }
            }
            attachments.add(new MailAttachmentResponse(
                    part.getBody().getAttachmentId(),
                    part.getFilename(),
                    part.getMimeType(),
                    contentId
            ));
        }
        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                extractAttachments(subPart, attachments);
            }
        }
    }

    @Override
    public byte[] getAttachment(String messageId, String attachmentId) throws IOException {
        String accessToken = SecurityUtils.getAccessToken("google");
        if (accessToken == null) {
            throw new IOException("Failed to authorize OAuth2 client or get access token");
        }

        MessagePartBody attachment = gmailClient.getAttachment(accessToken, messageId, attachmentId);

        return Base64.getUrlDecoder().decode(attachment.getData());
    }
}
