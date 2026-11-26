package com.gorson.springai.dto.response;

import java.util.List;

public record VectorStoreAddResponse(
        int insertedCount,
        List<String> ids
) {
}