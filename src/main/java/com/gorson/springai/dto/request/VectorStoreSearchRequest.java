package com.gorson.springai.dto.request;

public record VectorStoreSearchRequest(
        String query,
        Integer topK,
        String projectId,
        Double similarityThreshold
) {
}