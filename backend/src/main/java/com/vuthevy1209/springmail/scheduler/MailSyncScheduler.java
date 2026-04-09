package com.vuthevy1209.springmail.scheduler;

import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.mail.MailSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailSyncScheduler {

	private final UserRepository userRepository;
	private final MailSyncService mailSyncService;

	@Scheduled(fixedRate = 600000) // Every 10 minutes
	public void scheduleSync() {
		log.info("Starting scheduled mail sync...");
		List<User> users = userRepository.findAll();
		for (User user : users) {
			try {
				// Each sync call handles its own token check/refresh via SecurityUtils
				// Note: SecurityUtils.getAccessToken("google") works if the user session is active
				// For background sync without user session, we would need to store refresh tokens.
				// For now, this will only work for users with an active session in the background
				// or if we implement offline token management.
				mailSyncService.sync(user);
			} catch (IOException e) {
				log.error("Failed to sync mail for user {}", user.getEmail(), e);
			}
		}
	}
}
