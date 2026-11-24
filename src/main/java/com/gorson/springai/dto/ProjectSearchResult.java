package com.gorson.springai.dto;

import com.gorson.springai.service.ProjectQueryService;

import java.util.List;

public record ProjectSearchResult(
        long total,
        int pageNumber,
        int pageSize,
        List<ProjectRecord> records
) {
}
