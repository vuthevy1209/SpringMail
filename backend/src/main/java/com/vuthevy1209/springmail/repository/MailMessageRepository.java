package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailMessageRepository extends MongoRepository<MailMessage, String> {
    List<MailMessage> findByThreadId(String threadId);
    List<MailMessage> findByUserId(String userId);
    List<MailMessage> findByThreadIdOrderByInternalDateAsc(String threadId);
    void deleteByThreadId(String threadId);
    long countByThreadId(String threadId);

    @Query(value = "{ 'userId': ?0, 'labelIds': { $all: ?1 } }", sort = "{ 'internalDate': -1 }")
    List<MailMessage> findTopByUserIdAndLabelIdsContainsAll(String userId, List<String> labelIds, Pageable pageable);
}
