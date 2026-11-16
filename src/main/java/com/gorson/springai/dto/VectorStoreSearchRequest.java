package com.gorson.springai.dto;

public record VectorStoreSearchRequest(
        String query,
        Integer topK,
        String projectId,
        Double similarityThreshold
) {
}