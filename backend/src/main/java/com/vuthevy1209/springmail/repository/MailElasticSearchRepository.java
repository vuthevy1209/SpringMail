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

        @Query("""
        {
          "script_score": {
            "query": {
              "bool": {
                "must": [
                  { "term": { "userId": "?1" } }
                ],
                "should": [
                  {
                    "multi_match": {
                      "query": "?0",
                      "fields": [
                        "subject^2",
                        "bodyText",
                        "sender^1.5",
                        "senderEmail^1.5",
                        "receiver",
                        "receiverEmail"
                      ],
                      "type": "best_fields"
                    }
                  }
                ]
              }
            },
            "script": {
              "source": "_score + (doc.containsKey('contentVector') && doc['contentVector'].size() > 0 ? (cosineSimilarity(params.query_vector, 'contentVector') + 1.0) * 5.0 : 0.0)",
              "params": {
                "query_vector": ?2
              }
            }
          }
        }
    """)
    List<MailElasticSearch> searchHybrid(String keyword, String userId, List<Double> queryVector);
}
