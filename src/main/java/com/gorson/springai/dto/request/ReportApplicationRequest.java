package com.gorson.springai.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportApplicationRequest(
        @NotBlank String projectId,
        @NotBlank String question
) {
}