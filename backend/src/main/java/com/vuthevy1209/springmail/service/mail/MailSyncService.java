package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.entity.User;
import java.io.IOException;

public interface MailSyncService {
	void syncMail(User user, String accessToken) throws IOException;
}
