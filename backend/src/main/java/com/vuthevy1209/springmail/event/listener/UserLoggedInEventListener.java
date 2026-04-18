package com.vuthevy1209.springmail.event.listener;

import com.vuthevy1209.springmail.event.UserLoggedInEvent;
import com.vuthevy1209.springmail.service.gmail.GmailWatchService;
import com.vuthevy1209.springmail.service.mail.MailSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLoggedInEventListener {

    private final MailSyncService mailSyncService;
    private final GmailWatchService gmailWatchService;

    @Async
    @EventListener
    public void handleUserLoggedInEvent(UserLoggedInEvent event) {
        log.info("Handling login event for user: {}", event.getUser().getEmail());
        
        try {
            // 1. Setup Watch
            gmailWatchService.setupWatch(event.getUser(), event.getAccessToken());
            
            // 2. Sync Mail
            mailSyncService.syncMail(event.getUser(), event.getAccessToken());
            
        } catch (IOException e) {
            log.error("Error setting up watch or syncing mail for user: {}", event.getUser().getEmail(), e);
        }
    }
}
