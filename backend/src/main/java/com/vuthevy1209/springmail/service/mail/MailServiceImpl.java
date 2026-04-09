package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.gmail.GmailMapper;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

	private final GmailService gmailService;
	private final MailThreadRepository threadRepository;
	private final UserRepository userRepository;

	// -------------------------------------------------------------------------
	// Gmail label constants for DB filtering
	// -------------------------------------------------------------------------
	private static final String LABEL_INBOX = "INBOX";
	private static final String LABEL_SENT = "SENT";
	private static final String LABEL_DRAFTS = "DRAFT";
	private static final String LABEL_TRASH = "TRASH";
	private static final String LABEL_UNREAD = "UNREAD";
	private static final String LABEL_CATEGORY_PERSONAL = "CATEGORY_PERSONAL";
	private static final String LABEL_CATEGORY_PROMOTIONS = "CATEGORY_PROMOTIONS";
	private static final String LABEL_CATEGORY_SOCIAL = "CATEGORY_SOCIAL";
	private static final String LABEL_CATEGORY_UPDATES = "CATEGORY_UPDATES";

	@Override
	public List<MailThreadResponse> getRecentEmails(String folder, String category) throws IOException {

		// 1. Xác định userId hiện tại
		OAuth2User oauth2User = SecurityUtils.getCurrentOAuth2User();
		if (oauth2User == null) {
			throw new IOException("User not authenticated");
		}
		String email = oauth2User.getAttribute("email");
		var userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			throw new IOException("User not found in database: " + email);
		}
		String userId = userOpt.get().getId();

		// 2. Xác định label để filter
		String primaryLabel = resolvePrimaryLabel(folder);
		String categoryLabel = resolveCategoryLabel(category);

		// 3. Query từ DB
		List<MailThread> threads = queryThreadsFromDb(userId, primaryLabel, categoryLabel);

		// 4. Fallback về Gmail API nếu DB rỗng (chưa sync lần nào)
		if (threads.isEmpty()) {
			log.info("No threads in DB for user {}, falling back to Gmail API", email);
			return fetchFromGmailApi(folder, category);
		}

		// 5. Map sang MailThreadResponse (messages list rỗng cho danh sách)
		return threads.stream()
				.map(thread -> new MailThreadResponse(
						thread.getId(),
						thread.getSubject(),
						thread.getSnippet(),
						thread.getLastMessageTimestamp() != null
								? formatTimestamp(thread.getLastMessageTimestamp()) : null,
						thread.getLatestSenderName(),
						thread.getLabelIds() != null && thread.getLabelIds().contains(LABEL_UNREAD),
						thread.getMessageCount(),
						thread.getLastMessageTimestamp(),
						new ArrayList<>()
				))
				.toList();
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
			// Đảm bảo tin nhắn được sắp xếp chuẩn theo thời gian (cũ → mới)
			fullThread.getMessages().sort((m1, m2) -> Long.compare(m1.getInternalDate(), m2.getInternalDate()));
		}

		return GmailMapper.toMailThreadResponse(fullThread);
	}

	@Override
	public GmailAttachmentDto getAttachment(String messageId, String attachmentId, String filename, String mimeType) throws IOException {
		String accessToken = SecurityUtils.getAccessToken("google");
		if (accessToken == null) {
			throw new IOException("Failed to authorize OAuth2 client or get access token");
		}

		return gmailService.getAttachment(accessToken, messageId, attachmentId, filename, mimeType);
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	/**
	 * Query threads từ DB.
	 * Nếu có categoryLabel thì filter theo cả 2 (primaryLabel + categoryLabel).
	 * Lưu ý: MongoDB Spring Data không hỗ trợ 2 "contains" cùng lúc dễ dàng,
	 * nên lọc categoryLabel ở memory (số lượng nhỏ, vẫn OK).
	 */
	private List<MailThread> queryThreadsFromDb(String userId, String primaryLabel, String categoryLabel) {
		List<MailThread> threads;

		if (primaryLabel != null) {
			threads = threadRepository
					.findByUserIdAndLabelIdsContainingOrderByLastMessageTimestampDesc(userId, primaryLabel);
		} else {
			threads = threadRepository.findByUserIdOrderByLastMessageTimestampDesc(userId);
		}

		// In-memory filter theo category nếu có
		if (categoryLabel != null) {
			final String cat = categoryLabel;
			threads = threads.stream()
					.filter(t -> t.getLabelIds() != null && t.getLabelIds().contains(cat))
					.toList();
		}

		return threads;
	}

	/**
	 * Fallback: lấy thread list từ Gmail API khi DB chưa có data.
	 * Logic cũ giữ nguyên.
	 */
	private List<MailThreadResponse> fetchFromGmailApi(String folder, String category) throws IOException {
		String accessToken = SecurityUtils.getAccessToken("google");
		if (accessToken == null) {
			throw new IOException("Failed to authorize OAuth2 client or get access token");
		}

		String query = buildGmailQuery(folder, category);
		GmailListThreadsResponseDto response = gmailService.listThreads(accessToken, query, 20L, null);

		List<MailThreadResponse> threadResponses = new ArrayList<>();
		if (response.getThreads() != null) {
			for (GmailThreadDto threadSnippet : response.getThreads()) {
				GmailThreadDto fullThread = gmailService.getThread(
						accessToken, threadSnippet.getId(), "metadata",
						List.of("Subject", "Date", "From"));

				MailThreadResponse threadResponse = GmailMapper.toMailThreadResponse(fullThread);
				if (threadResponse != null) {
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

		threadResponses.sort((t1, t2) -> Long.compare(t2.internalDate(), t1.internalDate()));
		return threadResponses;
	}

	/**
	 * Map tên folder → Gmail label ID chuẩn.
	 */
	private String resolvePrimaryLabel(String folder) {
		if (folder == null) return LABEL_INBOX;
		return switch (folder.toLowerCase()) {
			case "sent"   -> LABEL_SENT;
			case "drafts" -> LABEL_DRAFTS;
			case "trash"  -> LABEL_TRASH;
			default       -> LABEL_INBOX;
		};
	}

	/**
	 * Map tên category → Gmail label ID chuẩn.
	 * Trả về null nếu không có category.
	 */
	private String resolveCategoryLabel(String category) {
		if (category == null || category.isBlank()) return null;
		return switch (category.toLowerCase()) {
			case "primary"    -> LABEL_CATEGORY_PERSONAL;
			case "promotions" -> LABEL_CATEGORY_PROMOTIONS;
			case "social"     -> LABEL_CATEGORY_SOCIAL;
			case "updates"    -> LABEL_CATEGORY_UPDATES;
			default           -> null;
		};
	}

	/**
	 * Xây dựng Gmail query string (dùng cho fallback API call).
	 */
	private String buildGmailQuery(String folder, String category) {
		String base = switch (folder != null ? folder.toLowerCase() : "inbox") {
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
	 * Format Unix timestamp (milliseconds) thành chuỗi ngày hiển thị.
	 * Trả về chuỗi ngắn gọn theo múi giờ hệ thống.
	 */
	private String formatTimestamp(long timestampMs) {
		java.time.Instant instant = java.time.Instant.ofEpochMilli(timestampMs);
		java.time.LocalDate today = java.time.LocalDate.now();
		java.time.LocalDate msgDate = instant
				.atZone(java.time.ZoneId.systemDefault())
				.toLocalDate();

		if (msgDate.equals(today)) {
			// Hôm nay: hiển thị giờ
			return java.time.format.DateTimeFormatter
					.ofPattern("HH:mm")
					.withZone(java.time.ZoneId.systemDefault())
					.format(instant);
		} else if (msgDate.getYear() == today.getYear()) {
			// Cùng năm: hiển thị ngày/tháng
			return java.time.format.DateTimeFormatter
					.ofPattern("d/M")
					.withZone(java.time.ZoneId.systemDefault())
					.format(instant);
		} else {
			// Năm khác: hiển thị ngày/tháng/năm
			return java.time.format.DateTimeFormatter
					.ofPattern("d/M/yyyy")
					.withZone(java.time.ZoneId.systemDefault())
					.format(instant);
		}
	}
}

