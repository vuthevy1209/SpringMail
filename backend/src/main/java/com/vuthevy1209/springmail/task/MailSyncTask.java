package com.vuthevy1209.springmail.task;

import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.gmail.GmailWatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailSyncTask {

	private final UserRepository userRepository;
	private final GmailWatchService gmailWatchService;
	private final OAuth2AuthorizedClientService authorizedClientService;

	/**
	 * Chạy định kỳ (mỗi ngày 1 lần) để renew lại Watch (Webhook) của Google
	 * Quét những người dùng có hạn Webhook <= 2 ngày thì tự động gia hạn (Refresh 7 ngày mới)
	 */
	@Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
	public void renewGmailWatch() {
		log.info("Starting scheduled renew for Gmail Webhook Watch");
		List<User> users = userRepository.findAll();

		// Tính mốc 2 ngày kể từ hiện tại, chuẩn bị đối chiếu
		Instant safetyThreshold = Instant.now().plus(2, ChronoUnit.DAYS);

		for (User user : users) {
			if (user.getWatchExpiration() == null) {
				continue;
			}

			// Nếu expired hoặc sắp expired trong vòng 2 ngày tới
			if (user.getWatchExpiration().isBefore(safetyThreshold)) {
				try {
					OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", user.getEmail());
					if (client != null && client.getAccessToken() != null) {
						String accessToken = client.getAccessToken().getTokenValue();
						gmailWatchService.setupWatch(user, accessToken);
						log.info("Successfully renewed Webhook watch for user {}", user.getEmail());
					} else {
						log.warn("Failed to renew watch for user {} - Missing OAuth2 Client Token", user.getEmail());
					}
				} catch (Exception e) {
					log.error("Error occurred while renewing watch for user {}", user.getEmail(), e);
				}
			}
		}
		log.info("Finished scheduled renew for Gmail Webhook Watch");
	}
}
