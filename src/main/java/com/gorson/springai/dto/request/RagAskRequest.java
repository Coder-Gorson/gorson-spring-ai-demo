package com.gorson.springai.dto.request;

public record RagAskRequest(
        String question,
        String projectId,
        Integer topK,
        Double similarityThreshold
) {
}

