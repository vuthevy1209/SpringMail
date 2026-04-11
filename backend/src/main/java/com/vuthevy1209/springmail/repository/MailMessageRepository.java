package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailMessageRepository extends MongoRepository<MailMessage, String> {
    List<MailMessage> findByThreadId(String threadId);
    List<MailMessage> findByUserId(String userId);
    List<MailMessage> findByThreadIdOrderByInternalDateAsc(String threadId);
    void deleteByThreadId(String threadId);
    long countByThreadId(String threadId);
}
