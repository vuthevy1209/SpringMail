package com.vuthevy1209.springmail.service.gmail.feign;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import feign.Response;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class GmailFeignConfiguration {

    @Bean
    public Decoder feignDecoder() {
        return new GmailFeignDecoder();
    }

    public static class GmailFeignDecoder implements Decoder {
        private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        @Override
        public Object decode(Response response, Type type) throws IOException {
            if (response.body() == null) {
                return null;
            }
            try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
                return jsonFactory.fromReader(reader, (Class<?>) type);
            }
        }
    }
}
