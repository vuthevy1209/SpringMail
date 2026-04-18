package com.vuthevy1209.springmail.service.mail.impl;

import com.vuthevy1209.springmail.dto.ai.AiSummaryResponse;
import com.vuthevy1209.springmail.dto.ai.AiDraftResponse;
import com.vuthevy1209.springmail.entity.MailChunkElasticSearch;
import com.vuthevy1209.springmail.repository.MailChunkElasticSearchRepository;
import com.vuthevy1209.springmail.service.cache.RedisCacheService;
import com.vuthevy1209.springmail.service.embedding.EmbeddingService;
import com.vuthevy1209.springmail.service.mail.MailAiService;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import com.vuthevy1209.springmail.dto.ai.UpcomingEventsResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailAiServiceImpl implements MailAiService {
    
    private final ChatClient chatClient;
    private final RedisCacheService redisCacheService;
    private final EmbeddingService embeddingService;

    private final MailChunkElasticSearchRepository mailChunkElasticSearchRepository;

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

    @Override
    public AiDraftResponse generateDraft(String threadId, String emailContent, String format) {
        String cacheKey = "email:draft:" + threadId + ":" + format;
        
        // 1. Kiểm tra cache trong Redis
        String cachedDraft = redisCacheService.getCachedValue(cacheKey);
        if (cachedDraft != null) {
            log.info("Return draft from Redis cache for threadId: {} with format: {}", threadId, format);
            return AiDraftResponse.builder()
                    .draftContent(cachedDraft)
                    .build();
        }

        log.info("Call Gemini AI to generate draft for threadId: {} with format: {}", threadId, format);
        
        String formatInstruction = "Kết quả kết xuất phải ở dạng ĐOẠN TEXT BÌNH THƯỜNG (plain text). Không được dùng Markdown hoặc HTML.";
        if ("markdown".equalsIgnoreCase(format)) {
            formatInstruction = "Kết quả BẮT BUỘC phải được định dạng bằng văn bản Markdown.";
        } else if ("html".equalsIgnoreCase(format)) {
            formatInstruction = "Kết quả BẮT BUỘC phải được định dạng bằng mã HTML. Không bọc trong ```html codeblock, chỉ trả nội dung mã.";
        }

        // 2. Nếu không có cache, gọi AI model
        String promptText = "Bạn là một trợ lý ảo chuyên nghiệp trong việc viết email. " +
                "Dựa vào email nhận được dưới đây (bao gồm nội dung gốc), " +
                "HÃY ĐÓNG VAI người nhận để phản hồi lại người gửi môt cách lịch sự, chuyên nghiệp, tự nhiên, " +
                "phản chiếu văn phong của email gốc. " +
                formatInstruction + " " +
                "Không cần giải thích thêm, chỉ trả về đúng nội dung draft của email. \n\n" +
                "Nội dung email nhận được:\n" + emailContent;

        String draft = chatClient.prompt()
                .user(promptText)
                .call()
                .content();

        // 3. Lưu vào Redis với thời gian sống (TTL) cho draft
        redisCacheService.cacheValue(cacheKey, draft, Duration.ofMinutes(5));

        return AiDraftResponse.builder()
                .draftContent(draft)
                .build();
    }

    @Override
    public UpcomingEventsResponse extractUpcomingEvents() {
        String userId = "118026135008790142027";

        String query = "Nội dung liên quan đến lịch hẹn như phỏng vấn, lịch kiểm tra, bài test, bạn bè rủ đi chơi, và có thời gian địa điểm rõ ràng";
        List<Float> contentVector = embeddingService.embed(query);

        List<MailChunkElasticSearch> mailChunkElasticSearchList = mailChunkElasticSearchRepository.getChunksByContentVector(contentVector, userId);

        String context = mailChunkElasticSearchList.stream()
                .map(MailChunkElasticSearch::getChunkText)
                .collect(Collectors.joining("\n\n"));

        String prompt = "Bạn là một trợ lý ảo phân tích dữ liệu chuyên nghiệp. Nhiệm vụ của bạn là đọc các đoạn nội dung email được cung cấp và trích xuất tất cả các sự kiện sắp diễn ra.\n\n" +
                "Các sự kiện cần quan tâm: lịch phỏng vấn, lịch kiểm tra/bài test, cuộc hẹn công việc, lịch rủ đi chơi, hoặc bất kỳ sự kiện nào có đề cập đến thời gian và địa điểm rõ ràng.\n\n" +
                "Với mỗi sự kiện, hãy trích xuất các thông tin sau để điền vào đối tượng EventDto:\n" +
                "- title: Tên hoặc tiêu đề của sự kiện (ngắn gọn, rõ ý).\n" +
                "- datetime: Thời gian diễn ra sự kiện (ngày, giờ). Hãy trích xuất chính xác theo những gì được đề cập trong email.\n" +
                "- location: Địa điểm diễn ra sự kiện (ghi rõ tên địa điểm, địa chỉ, hoặc link meeting nếu là sự kiện online).\n" +
                "- description: Mô tả ngắn gọn, chi tiết bổ sung hoặc các yêu cầu chuẩn bị cần thiết cho sự kiện.\n" +
                "- status: Trạng thái của sự kiện (ví dụ: 'Upcoming', 'TENTATIVE', 'CONFIRMED').\n\n" +
                "Ngoài ra, hãy cung cấp một đoạn phân tích tổng quan (vào trường rawAnalysis) tóm tắt nhanh về tình hình các sự kiện bạn tìm thấy hoặc lý do tại sao bạn trích xuất chúng.\n\n" +
                "Nội dung email:\n" + context;

        log.info("Call Gemini AI to extract upcoming events for userId: {}", userId);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(UpcomingEventsResponse.class);
    }
}
