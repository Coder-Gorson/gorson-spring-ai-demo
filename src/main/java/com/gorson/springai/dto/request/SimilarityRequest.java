package com.gorson.springai.dto.request;

import java.util.List;

public record SimilarityRequest(
        List<String> texts
) {
}