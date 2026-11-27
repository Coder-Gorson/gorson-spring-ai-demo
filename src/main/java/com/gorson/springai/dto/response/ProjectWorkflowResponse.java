package com.gorson.springai.dto.response;

import com.gorson.springai.dto.result.ProjectSearchResult;

public record ProjectWorkflowResponse(
        Status status,
        ProjectSearchResult projects,
        RagAskResponse answer
) {
    public enum Status {
        PROJECT_NOT_FOUND,
        SELECTION_REQUIRED,
        ANSWERED
    }
}