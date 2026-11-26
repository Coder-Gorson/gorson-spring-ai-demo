package com.gorson.springai.dto.result;

import java.util.Map;

public record KnowledgeSearchResult(

        int index,

        String documentId,

        String content,

        Map<String, Object> metadata

) {
}