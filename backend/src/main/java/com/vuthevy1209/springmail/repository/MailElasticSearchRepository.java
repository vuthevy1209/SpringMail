package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailElasticSearch;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
    public interface MailElasticSearchRepository extends ElasticsearchRepository<MailElasticSearch, String> {
        List<MailElasticSearch> findBySender(String senderEmail);

        List<MailElasticSearch> findBySubjectContainingOrBodyTextContaining(String subjectKeyword, String bodyKeyword);

        @Query("""
            {
                "multi_match": {
                    "query": "?0",
                    "fields": [
                        "subject",
                        "bodyText",
                        "sender^2",
                        "senderEmail^2",
                        "receiver^2",
                        "receiverEmail^2"
                    ],
                    "type": "cross_fields",
                    "operator": "and"
                }
            }
            """)
        List<MailElasticSearch> searchByKeyword(String keyword);
}
