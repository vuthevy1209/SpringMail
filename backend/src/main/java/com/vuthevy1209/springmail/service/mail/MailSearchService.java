package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.mail.request.MailThreadsRequest;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.entity.MailElasticSearch;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MailSearchService {
    void save(MailElasticSearch email);
    Page<MailThreadResponse> searchEmails(String keyword, int page, int size);
}
