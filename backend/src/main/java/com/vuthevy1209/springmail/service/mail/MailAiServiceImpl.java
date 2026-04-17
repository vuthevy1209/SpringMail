package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.AiSummaryResponse;
import com.vuthevy1209.springmail.service.cache.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class MailAiServiceImpl implements MailAiService {
    
    private final ChatClient chatClient;
    private final RedisCacheService redisCacheService;

    public MailAiServiceImpl(ChatClient.Builder builder, RedisCacheService redisCacheService) {
        this.chatClient = builder.build();
        this.redisCacheService = redisCacheService;
    }

    @Override
    public AiSummaryResponse summarize(String threadId, String emailContent) {
        String cacheKey = "email:summary:" + threadId;
        
        // 1. Kiểm tra cache trong Redis
        String cachedSummary = redisCacheService.getCachedValue(cacheKey);
        if (cachedSummary != null) {
            log.info("Return summary from Redis cache for threadId: {}", threadId);
            return AiSummaryResponse.builder()
                    .markdownSummary(cachedSummary)
                    .build();
        }

        log.info("Call Gemini AI to summarize for threadId: {}", threadId);
        // 2. Nếu không có cache, gọi AI model
        String promptText = "Bạn là một trợ lý ảo phân tích email chuyên nghiệp. " +
                "Hãy tóm tắt ngắn gọn, mạch lạc nội dung email sau bằng tiếng Việt. " +
                "Kết quả bắt buộc phải được định dạng bằng Markdown. " +
                "Hãy sử dụng bullet points để liệt kê các ý chính quan trọng nhất, " +
                "in đậm các từ khóa, mốc thời gian, tên người, hoặc các chi tiết hành động cần lưu ý.\n\n" +
                "Nội dung email:\n" + emailContent;

        String summary = chatClient.prompt()
                .user(promptText)
                .call()
                .content();

        // 3. Lưu vào Redis với thời gian sống (TTL) là 3 phút
        redisCacheService.cacheValue(cacheKey, summary, Duration.ofMinutes(3));

        return AiSummaryResponse.builder()
                .markdownSummary(summary)
                .build();
    }
}