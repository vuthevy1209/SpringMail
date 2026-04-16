package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.dto.mail.request.FetchOlderRequest;
import com.vuthevy1209.springmail.dto.mail.response.FetchOlderResponse;
import java.io.IOException;

public interface MailSyncService {
        void syncMail(User user, String accessToken) throws IOException;
        FetchOlderResponse fetchOlderThreads(FetchOlderRequest request) throws IOException;
        void processNewEmails(String email, String historyId);
}
