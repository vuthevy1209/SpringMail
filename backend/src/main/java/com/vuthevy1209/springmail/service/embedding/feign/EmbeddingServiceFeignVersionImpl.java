package com.vuthevy1209.springmail.service.embedding.feign;

import com.vuthevy1209.springmail.service.embedding.EmbeddingService;
import com.vuthevy1209.springmail.service.embedding.dto.TextRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class EmbeddingServiceFeignVersionImpl implements EmbeddingService {

    private final EmbeddingFeignClient embeddingFeignClient;

    @Override
    public List<Double> embed(String text) {
        TextRequest request = TextRequest.builder()
                .text(text)
                .build();

        return embeddingFeignClient.embed(request);
    }
}
