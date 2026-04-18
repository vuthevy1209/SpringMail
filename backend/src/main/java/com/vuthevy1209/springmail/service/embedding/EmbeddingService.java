package com.vuthevy1209.springmail.service.embedding;

import java.util.List;

public interface EmbeddingService {
    List<Float> embed(String text);
    List<List<Float>> embedBatch(List<String> texts);
}
