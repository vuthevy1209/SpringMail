package com.vuthevy1209.springmail.repository.custom;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.vuthevy1209.springmail.entity.MailChunkElasticSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MailChunkElasticSearchRepositoryCustomImpl implements MailChunkElasticSearchRepositoryCustom {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public List<MailChunkElasticSearch> getChunksByContentVector(List<Float> contentVector, String userId) {

        try {
            SearchResponse<MailChunkElasticSearch> response = elasticsearchClient.search(s -> s
                            .index("email_chunks")
                            .size(20)
                            .knn(knn -> knn
                                    .field("contentVector")
                                    .queryVector(contentVector)
                                    .k(20)
                                    .numCandidates(200)
                                    .filter(f -> f
                                            .bool(b -> b
                                                    .must(m -> m.term(t -> t
                                                            .field("userId")
                                                            .value(userId)
                                                    ))
                                                    .must(m -> m.term(t -> t
                                                            .field("labelIds")
                                                            .value("INBOX")
                                                    ))
                                                    .must(m -> m.term(t -> t
                                                            .field("labelIds")
                                                            .value("CATEGORY_PERSONAL")
                                                    ))
                                            )
                                    )
                            ),
                    MailChunkElasticSearch.class
            );

            List<MailChunkElasticSearch> results = new ArrayList<>();

            for (Hit<MailChunkElasticSearch> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }

            return results;

        } catch (IOException e) {
            throw new RuntimeException("Error executing KNN search", e);
        }
    }
}
