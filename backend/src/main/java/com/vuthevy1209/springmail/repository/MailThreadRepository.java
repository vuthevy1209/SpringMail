package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailThread;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailThreadRepository extends MongoRepository<MailThread, String> {
    List<MailThread> findByUserId(String userId);
}
