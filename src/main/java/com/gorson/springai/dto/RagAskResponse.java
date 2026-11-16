package com.gorson.springai.dto;

import java.util.List;

public record RagAskResponse(
        String question,
        String answer,
        int retrievedCount,
        List<SourceReference> sources
) {

    public record SourceReference(
            String reference,
            String documentId,
            String source,
            Integer page
    ) {
    }
}