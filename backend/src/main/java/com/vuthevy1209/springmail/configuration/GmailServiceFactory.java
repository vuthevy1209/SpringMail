package com.vuthevy1209.springmail.configuration;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import org.springframework.stereotype.Component;

@Component
public class GmailServiceFactory {

	private static final String APPLICATION_NAME = "SpringMail";

	public Gmail build(String accessToken) {
		HttpRequestInitializer httpRequestInitializer = request ->
			request.getHeaders().setAuthorization("Bearer " + accessToken);

		return new Gmail.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), httpRequestInitializer)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}
}
