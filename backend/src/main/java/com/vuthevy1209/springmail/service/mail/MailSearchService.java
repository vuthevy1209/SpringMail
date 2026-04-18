package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.entity.MailElasticSearch;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MailSearchService {
    Page<MailThreadResponse> searchEmails(String keyword, int page, int size);
    Page<MailThreadResponse> searchEmailsHybrid(String keyword, int page, int size);
    List<String> suggestSubjects(String keyword);
}
