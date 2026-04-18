package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailElasticSearch;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailElasticSearchRepository extends ElasticsearchRepository<MailElasticSearch, String> {
    @Query("""
        {
            "bool": {
                "must": [
                    {
                        "term": { "userId": "?1" }
                    },
                    {
                        "multi_match": {
                            "query": "?0",
                            "fields": [
                                "subject",
                                "snippet",
                                "sender^2",
                                "senderEmail^2",
                                "receiver^2",
                                "receiverEmail^2"
                            ],
                            "type": "cross_fields",
                            "operator": "and"
                        }
                    }
                ]
            }
        }
        """)
    List<MailElasticSearch> searchByKeyword(String keyword, String userId);

    @Query("""
        {
            "bool": {
                "must": [
                    { "term": { "userId": "?1" } }
                ],
                "should": [
                    {
                        "match_phrase_prefix": {
                            "subject": {
                                "query": "?0",
                                "max_expansions": 10
                            }
                        }
                    },
                    {
                        "match_phrase_prefix": {
                            "sender": {
                                "query": "?0",
                                "max_expansions": 10
                            }
                        }
                    },
                    {
                        "match_phrase_prefix": {
                            "snippet": {
                                "query": "?0",
                                "max_expansions": 10
                            }
                        }
                    },
                    {
                        "wildcard": {
                            "senderEmail": {
                                "value": "*?0*"
                            }
                        }
                    }
                ],
                "minimum_should_match": 1
            }
        }
    """)
    List<MailElasticSearch> suggestSubjects(String prefix, String userId);
}
