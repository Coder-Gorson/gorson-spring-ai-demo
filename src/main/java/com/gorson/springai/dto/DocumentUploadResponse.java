package com.gorson.springai.dto;

import java.util.List;
import java.util.Map;

public record DocumentUploadResponse(

        String filename,

        long fileSize,

        int documentCount,

        List<DocumentItem> documents

) {

    public record DocumentItem(

            String id,

            int textLength,

            String textPreview,

            Map<String, Object> metadata

    ) {
    }
}