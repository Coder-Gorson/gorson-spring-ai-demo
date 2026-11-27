package com.gorson.springai.dto.response;

public record ReportApprovalResponse(
        String reportId,
        String status,
        String report
) {
}