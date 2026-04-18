package com.vuthevy1209.springmail.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    /**
     * Executor riêng cho luồng sync mail bất đồng bộ.
     *
     * <p>Pool size nhỏ để không làm quá tải BERT embedding server.
     * Mỗi task = lưu 1 message vào MongoDB + Elasticsearch (bao gồm gọi /embed/batch).
     * Với pool = 3, tối đa 3 request embed chạy song song tại một thời điểm.</p>
     *
     * <p>Queue capacity = 500 đủ để buffer toàn bộ initial sync (50 threads × ~10 messages).
     * Nếu queue đầy, caller sẽ bị block (CallerRunsPolicy) thay vì bị reject.</p>
     */
    @Bean(name = "mailSyncExecutor")
    public Executor mailSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("mail-sync-");
        // Khi queue đầy, thread gọi sẽ tự chạy task — tạo back-pressure tự nhiên
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
