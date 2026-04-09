package com.vuthevy1209.springmail.task;

import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.mail.MailSyncService;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailSyncTask {

	private final UserRepository userRepository;
	private final MailSyncService mailSyncService;

	/**
	 * Chạy incremental sync cho tất cả người dùng mỗi 15 phút.
	 */
	@Scheduled(fixedDelay = 15 * 60 * 1000)
	public void scheduleIncrementalSync() {
		log.info("Starting scheduled incremental sync for all users");
		List<User> users = userRepository.findAll();

		for (User user : users) {
			if (user.getLastHistoryId() == null) {
				log.info("Skipping background sync for user {} (no initial sync yet)", user.getEmail());
				continue;
			}

			try {
				String accessToken = SecurityUtils.getAccessTokenForUser("google", user.getEmail());
				if (accessToken != null) {
					mailSyncService.syncMail(user, accessToken);
					log.info("Background sync completed for user {}", user.getEmail());
				} else {
					log.warn("Could not retrieve access token for background sync for user {}", user.getEmail());
				}
			} catch (Exception e) {
				log.error("Failed to perform background sync for user {}", user.getEmail(), e);
			}
		}
		log.info("Finished scheduled incremental sync");
	}
}
