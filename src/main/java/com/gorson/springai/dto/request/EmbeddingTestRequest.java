package com.gorson.springai.dto.request;

import java.util.List;

public record EmbeddingTestRequest(
        List<String> texts
) {
}