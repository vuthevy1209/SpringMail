package com.vuthevy1209.springmail.event;

import com.vuthevy1209.springmail.entity.MailMessage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.concurrent.CompletableFuture;

@Getter
public class SyncMailMessageEvent extends ApplicationEvent {
    private final MailMessage mailMessage;

    /**
     * Future được complete bởi listener sau khi lưu message vào MongoDB/ES.
     * Cho phép publisher theo dõi tiến độ xử lý thực tế (không chỉ "đã publish").
     */
    private final CompletableFuture<Void> processingFuture;

    public SyncMailMessageEvent(Object source, MailMessage mailMessage) {
        super(source);
        this.mailMessage = mailMessage;
        this.processingFuture = new CompletableFuture<>();
    }
}
