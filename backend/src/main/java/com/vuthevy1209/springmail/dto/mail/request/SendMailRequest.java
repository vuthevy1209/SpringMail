package com.vuthevy1209.springmail.dto.mail.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMailRequest {
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String body;
    
    // Các fields dành cho tính năng Reply
    private String threadId;
    private String inReplyTo; // message-id của mail được reply
    
    private List<MultipartFile> attachments; // Trường dành cho file đính kèm
}
