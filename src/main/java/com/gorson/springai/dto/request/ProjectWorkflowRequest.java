package com.gorson.springai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectWorkflowRequest(
        @NotBlank @Size(max = 100) String projectKeyword,
        @NotBlank String question
) {
}