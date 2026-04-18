package com.vuthevy1209.springmail.event.listener;

import com.vuthevy1209.springmail.converters.MailMessageConverter;
import com.vuthevy1209.springmail.entity.MailChunkElasticSearch;
import com.vuthevy1209.springmail.entity.MailElasticSearch;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.event.SyncMailMessageEvent;
import com.vuthevy1209.springmail.repository.MailChunkElasticSearchRepository;
import com.vuthevy1209.springmail.repository.MailElasticSearchRepository;
import com.vuthevy1209.springmail.repository.MailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncMailMessageListener {

    private final MailMessageRepository mailMessageRepository;
    private final MailElasticSearchRepository mailElasticSearchRepository;
    private final MailChunkElasticSearchRepository mailChunkElasticSearchRepository;
    private final MailMessageConverter mailMessageConverter;

    @Async("mailSyncExecutor")
    @EventListener
    public void onSyncMailMessageEvent(SyncMailMessageEvent event) {
        MailMessage mailMessageEntity = event.getMailMessage();

        try {
            // save message to mongodb
            mailMessageRepository.save(mailMessageEntity);

            // save to elasticsearch 
            if (!mailElasticSearchRepository.existsById(mailMessageEntity.getId())) {
                MailElasticSearch mailElasticSearch = mailMessageConverter.toMailElasticSearch(mailMessageEntity);
                mailElasticSearchRepository.save(mailElasticSearch);
            }

            // save to elasticsearch chunk 
            if (!mailChunkElasticSearchRepository.existsById(mailMessageEntity.getId())) {
                List<MailChunkElasticSearch> chunks = mailMessageConverter.toMailChunksElasticSearch(mailMessageEntity);
                if (!chunks.isEmpty()) {
                    mailChunkElasticSearchRepository.saveAll(chunks);
                }
            }

            // Báo cho publisher biết message đã được xử lý xong
            event.getProcessingFuture().complete(null);
        } catch (Exception e) {
            log.error("Error occurred while processing message asynchronously for message id: {}", mailMessageEntity.getId(), e);
            event.getProcessingFuture().completeExceptionally(e);
        }
    }
}
