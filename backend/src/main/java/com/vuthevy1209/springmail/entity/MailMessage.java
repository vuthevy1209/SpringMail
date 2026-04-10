package com.vuthevy1209.springmail.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;

import java.util.List;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(collection = "mail_messages")
public class MailMessage {

    private String userId;

    @Id
    private String id; // Gmail Message ID
    private String threadId;
    private String snippet;
    private Long internalDate;
    private Long historyId;
    private Set<String> labelIds;
    
    private String fromEmail;
	private String fromName;
	private String toEmail;
	private String toName;
	private List<String> cc;
	private List<String> bcc;
	private String subject;
    private String dateString;
    
    private String bodyHtml;
    private String bodyText;
    private Long sizeEstimate;
    
    private List<GmailAttachmentDto> attachments;
}
