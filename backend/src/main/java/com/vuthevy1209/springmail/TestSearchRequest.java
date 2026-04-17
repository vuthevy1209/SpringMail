package com.vuthevy1209.springmail;
import org.springframework.ai.vectorstore.SearchRequest;
public class TestSearchRequest {
    public void test() {
        SearchRequest req = SearchRequest.builder().query("abc").build();
    }
}
