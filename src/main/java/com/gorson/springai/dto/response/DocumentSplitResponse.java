package com.gorson.springai.dto.response;

import java.util.List;
import java.util.Map;

public record DocumentSplitResponse(

        String filename,

        long fileSize,

        int originalDocumentCount,

        int chunkCount,

        List<ChunkItem> chunks

) {

    public record ChunkItem(

            String id,

            int textLength,

            String text,

            Map<String, Object> metadata

    ) {
    }
}