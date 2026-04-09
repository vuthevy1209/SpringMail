package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.entity.User;
import java.io.IOException;

public interface MailSyncService {
	void syncForUser() throws IOException;

	void syncForUser(User user) throws IOException;

	void syncMail(User user, String accessToken) throws IOException;
}
