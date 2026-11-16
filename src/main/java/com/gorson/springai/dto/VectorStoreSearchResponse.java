package com.gorson.springai.dto;

import java.util.List;
import java.util.Map;

public record VectorStoreSearchResponse(
        String query,
        int resultCount,
        List<SearchResult> results
) {

    public record SearchResult(
            String id,
            String text,
            Map<String, Object> metadata,
            Double score
    ) {
    }
}