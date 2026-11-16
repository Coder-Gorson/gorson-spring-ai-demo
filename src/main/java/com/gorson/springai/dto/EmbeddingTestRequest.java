package com.gorson.springai.dto;

import java.util.List;

public record EmbeddingTestRequest(
        List<String> texts
) {
}