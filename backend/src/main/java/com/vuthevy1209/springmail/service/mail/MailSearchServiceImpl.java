package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.converters.MailThreadConverter;
import com.vuthevy1209.springmail.dto.mail.request.MailThreadsRequest;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.entity.MailElasticSearch;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.repository.MailElasticSearchRepository;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailSearchServiceImpl implements MailSearchService {
    private final MailElasticSearchRepository emailElasticSearchRepository;
    private final MailThreadRepository mailThreadRepository;

    private final MailThreadConverter mailThreadConverter;

    @Override
    public void save(MailElasticSearch email) {
        emailElasticSearchRepository.save(email);
    }

    @Override
    public Page<MailThreadResponse> searchEmails(String keyword, int page, int size) {
        OAuth2User user = SecurityUtils.getCurrentOAuth2User();
        if (user == null) {
            throw new RuntimeException("Current user not found");
        }

        String userId = user.getAttribute("googleId");

        List<MailElasticSearch> mailElasticSearches =
                emailElasticSearchRepository.searchByKeyword(keyword);

        List<String> threadIds = mailElasticSearches.stream()
                .map(MailElasticSearch::getThreadId)
                .distinct()
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageTimestamp").descending());
        
        if (threadIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<MailThread> threads = mailThreadRepository.findByUserIdAndIdIn(userId, threadIds, pageable);

        // Map trả về Page<MailThreadResponse>
        return threads.map(mailThreadConverter::toMailThreadResponse);
    }
}
