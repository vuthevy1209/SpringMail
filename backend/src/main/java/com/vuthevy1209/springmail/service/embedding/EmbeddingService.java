package com.vuthevy1209.springmail.service.embedding;

import java.util.List;

public interface EmbeddingService {
    List<Double> embed(String text);
    List<List<Double>> embedBatch(List<String> texts);
}
