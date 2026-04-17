package com.vuthevy1209.springmail.service.mail.impl;

import com.vuthevy1209.springmail.dto.ai.AiSummaryResponse;
import com.vuthevy1209.springmail.dto.ai.AiDraftResponse;
import com.vuthevy1209.springmail.dto.ai.MailVectorDto;
import com.vuthevy1209.springmail.service.cache.RedisCacheService;
import com.vuthevy1209.springmail.service.mail.MailAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.vuthevy1209.springmail.dto.ai.UpcomingEventsResponse;
import org.springframework.ai.converter.BeanOutputConverter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailAiServiceImpl implements MailAiService {
    
    private final ChatClient chatClient;
    private final RedisCacheService redisCacheService;
    private final VectorStore vectorStore;

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
    public void saveMailToVectorStore(MailVectorDto mailVectorDto) {
        String contentToEmbed = String.format("Người gửi (From): %s\nTiêu đề (Subject): %s\nThời gian (Date): %s\nNội dung (Content): %s",
                mailVectorDto.getSender(), mailVectorDto.getSubject(), mailVectorDto.getDateStr(), mailVectorDto.getContent());

        Map<String, Object> metadata = Map.of(
                "mailId", mailVectorDto.getMailId(),
                "threadId", mailVectorDto.getThreadId() != null ? mailVectorDto.getThreadId() : "",
                "userId", mailVectorDto.getUserId(),
                "subject", mailVectorDto.getSubject(),
                "sender", mailVectorDto.getSender(),
                "date", mailVectorDto.getDateStr(),
                "timestamp", mailVectorDto.getTimestamp() != null ? mailVectorDto.getTimestamp() : 0L
        );

        Document document = new Document(contentToEmbed, metadata);
        vectorStore.add(List.of(document));
    }

    @Override
    public UpcomingEventsResponse extractUpcomingEvents(String userId) {
        // 1. Tìm kiếm semantic search cho các sự kiện trong 2 tuần qua
        String searchQuery = "Sự kiện, cuộc họp, phỏng vấn, lịch hẹn, chuyến bay, event, meeting, interview, appointment, flight, vé máy bay, thư mời, schedule, lích trình";
        
        long twoWeeksAgo = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000);
        
        SearchRequest searchRequest = SearchRequest.builder()
                .query(searchQuery)
                .topK(20) // Tăng lên 20 để lấy thêm ngữ cảnh từ vector store
                .filterExpression("userId == '" + userId + "' && timestamp >= " + twoWeeksAgo)
                .build();
                
        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        StringBuilder contentBuilder = new StringBuilder();

        // Thêm mail từ Vector Store
        for (Document doc : similarDocuments) {
            String threadId = (String) doc.getMetadata().getOrDefault("threadId", "");
            contentBuilder.append("Email (ThreadID: ").append(threadId).append("):\n")
                    .append(doc.getText()).append("\n\n");
        }

        if (contentBuilder.isEmpty()) {
            return UpcomingEventsResponse.builder().events(List.of()).build();
        }

        var outputConverter = new BeanOutputConverter<>(UpcomingEventsResponse.class);

        String promptText = "Bạn là một trợ lý ảo chuyên tìm kiếm và trích xuất thông tin từ email. " +
                "Dựa vào nội dung của các email dưới đây, hãy trích xuất danh sách các sự kiện sắp tới (upcoming events). " +
                "Mỗi sự kiện phải có: title, date (ngày giờ sự kiện), location, description và threadId của email đó. " +
                "Nếu không có sự kiện nào rõ ràng, hãy trả về mảng rỗng.\n\n" +
                "Lưu ý: Phải trả ra đúng định dạng JSON như hướng dẫn bên dưới.\n\n" +
                outputConverter.getFormat() + "\n\n" +
                "Nội dung các email:\n" + contentBuilder.toString();

        String response = chatClient.prompt()
                .user(promptText)
                .call()
                .content();

        return outputConverter.convert(response);
    }
    
    @Override
    public void deleteMailFromVectorStore(String mailId) {
        vectorStore.delete(List.of(mailId));
    }
}
