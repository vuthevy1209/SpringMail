package com.vuthevy1209.springmail.service.embedding.feign;

import com.vuthevy1209.springmail.service.embedding.dto.BatchTextRequest;
import com.vuthevy1209.springmail.service.embedding.dto.TextRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "embeddingFeignClient", url = "http://localhost:8000")
public interface EmbeddingFeignClient {

    @PostMapping("/embed")
    List<Float> embed(@RequestBody TextRequest request);

    @PostMapping("/embed/batch")
    List<List<Float>> embedBatch(@RequestBody BatchTextRequest request);
}
