package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailElasticSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailElasticSearchRepository extends ElasticsearchRepository<MailElasticSearch, String> {
    List<MailElasticSearch> findBySender(String senderEmail);

    List<MailElasticSearch> findBySubjectContainingOrBodyTextContaining(String subjectKeyword, String bodyKeyword);

}
