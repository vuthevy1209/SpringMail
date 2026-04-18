package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailChunkElasticSearch;
import com.vuthevy1209.springmail.repository.custom.MailChunkElasticSearchRepositoryCustom;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailChunkElasticSearchRepository extends ElasticsearchRepository<MailChunkElasticSearch, String>, MailChunkElasticSearchRepositoryCustom {
}