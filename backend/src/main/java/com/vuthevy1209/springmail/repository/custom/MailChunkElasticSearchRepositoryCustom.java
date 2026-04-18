package com.vuthevy1209.springmail.repository.custom;

import com.vuthevy1209.springmail.entity.MailChunkElasticSearch;

import java.util.List;

public interface MailChunkElasticSearchRepositoryCustom {
    List<MailChunkElasticSearch> getChunksByContentVector(List<Float> contentVector, String userId);
}
