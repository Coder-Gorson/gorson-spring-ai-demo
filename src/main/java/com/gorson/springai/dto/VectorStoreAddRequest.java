package com.gorson.springai.dto;

import java.util.List;

public record VectorStoreAddRequest(
        List<ChunkInput> documents
) {

    public record ChunkInput(
            String id,
            String text,
            String projectId,
            String source,
            Integer page
    ) {
    }
}