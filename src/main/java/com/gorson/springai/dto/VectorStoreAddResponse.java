package com.gorson.springai.dto;

import java.util.List;

public record VectorStoreAddResponse(
        int insertedCount,
        List<String> ids
) {
}