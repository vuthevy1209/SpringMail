package com.vuthevy1209.springmail.service.gmail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import com.vuthevy1209.springmail.configuration.GmailServiceFactory;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailWatchService {

    private final GmailServiceFactory gmailServiceFactory;
    private final UserRepository userRepository;

    public void setupWatch(User user, String accessToken) throws IOException {
        // Tạo Gmail Client bằng Access Token
        Gmail gmailClient = gmailServiceFactory.build(accessToken);

        WatchRequest watchRequest = new WatchRequest()
                .setTopicName("projects/springmail-492205/topics/spring-mail-notification");

        // "me" nghĩa là thay mặt user đang sở hữu Access Token
        WatchResponse response = gmailClient.users()
                .watch("me", watchRequest)
                .execute();

        log.info("Setup watch successfully for User: {}, Expiration: {}, History ID: {}",
                user.getEmail(), response.getExpiration(), response.getHistoryId());

        // Lưu thời gian hết hạn (Expiration - Tính bằng milli-seconds) vào Database
        if (response.getExpiration() != null) {
            user.setWatchExpiration(Instant.ofEpochMilli(response.getExpiration()));
            
            // Nếu là lần đầu tiên chưa có historyId thì mới lấy mốc từ Watch, nếu có rồi cứ để hàm SyncMail xử lý
            if (user.getLastHistoryId() == null && response.getHistoryId() != null) {
                user.setLastHistoryId(response.getHistoryId().longValue());
            }
            userRepository.save(user);
        }
    }
}