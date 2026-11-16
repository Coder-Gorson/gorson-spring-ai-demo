package com.gorson.springai.dto;

public record RagAskRequest(
        String question,
        String projectId,
        Integer topK,
        Double similarityThreshold
) {
}