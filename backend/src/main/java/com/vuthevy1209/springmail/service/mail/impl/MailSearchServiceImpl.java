package com.vuthevy1209.springmail.service.mail.impl;

import com.vuthevy1209.springmail.converters.MailThreadConverter;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.entity.MailElasticSearch;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.repository.MailElasticSearchRepository;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.service.embedding.EmbeddingService;
import com.vuthevy1209.springmail.service.mail.MailSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSearchServiceImpl implements MailSearchService {

    private final MailElasticSearchRepository mailElasticSearchRepository;
    private final MailThreadRepository mailThreadRepository;
    private final MailThreadConverter mailThreadConverter;
    private final EmbeddingService embeddingService;

    @Override
    public Page<MailThreadResponse> searchEmails(String keyword, int page, int size) {
        String userId = SecurityUtils.getAuthenticatedUserId();
        if (userId == null) {
            throw new RuntimeException("Current user not found");
        }

        // search in elastic
        List<MailElasticSearch> results = mailElasticSearchRepository.searchByKeyword(keyword, userId);
        List<String> threadIds = results.stream()
                .map(MailElasticSearch::getThreadId)
                .toList();

        Pageable pageable = PageRequest.of(page, size);
        Page<MailThread> mailThreads = mailThreadRepository.findByIdIn(threadIds, pageable);

        // convert to mail thread response list.
        return mailThreads.map(mailThreadConverter::toMailThreadResponse);
    }


    @Override
    public List<String> suggestSubjects(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String userId = SecurityUtils.getAuthenticatedUserId();
        if (userId == null) {
            throw new RuntimeException("Current user not found");
        }
        
        String lowerKeyword = keyword.toLowerCase();
        List<MailElasticSearch> results = mailElasticSearchRepository.suggestSubjects(keyword, userId);
        
        return results.stream()
                .flatMap(es -> Stream.of(es.getSubject(), es.getSender(), es.getSenderEmail()))
                .filter(Objects::nonNull)
                .filter(text -> text.toLowerCase().contains(lowerKeyword))
                .distinct()
                .limit(8)
                .toList();
    }
}
