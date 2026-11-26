package com.gorson.springai.dto.result;

import com.gorson.springai.dto.ProjectRecord;

import java.util.List;

public record ProjectSearchResult(
        long total,
        int pageNumber,
        int pageSize,
        List<ProjectRecord> records
) {
}
