package com.gorson.springai.dto;

import java.util.List;

public record VectorStoreReplaceRequest(
        String projectId,
        String source,
        List<ChunkInput> documents
) {

    public record ChunkInput(
            String id,
            String text,
            Integer page
    ) {
    }
}