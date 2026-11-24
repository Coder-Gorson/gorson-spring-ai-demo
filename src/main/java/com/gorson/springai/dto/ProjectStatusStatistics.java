package com.gorson.springai.dto;

import java.util.Map;

public record ProjectStatusStatistics(
        int totalProjects,
        Map<String, Long> statusCounts
) {
}
