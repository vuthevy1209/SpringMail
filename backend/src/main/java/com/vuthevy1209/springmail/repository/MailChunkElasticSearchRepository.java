package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailChunkElasticSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailChunkElasticSearchRepository extends ElasticsearchRepository<MailChunkElasticSearch, String> {
}