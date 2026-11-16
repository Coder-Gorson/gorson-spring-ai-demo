package com.gorson.springai.dto;

import java.util.List;

public record SimilarityRequest(
        List<String> texts
) {
}