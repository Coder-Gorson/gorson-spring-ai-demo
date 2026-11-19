package com.gorson.springai.dto.tool;

import java.util.Map;

public record KnowledgeSearchResult(

        int index,

        String documentId,

        String content,

        Map<String, Object> metadata

) {
}