package com.gorson.springai.service;

import com.gorson.springai.dto.request.ProjectSearchRequest;
import com.gorson.springai.dto.request.ProjectWorkflowRequest;
import com.gorson.springai.dto.response.ProjectWorkflowResponse;
import com.gorson.springai.dto.response.ProjectWorkflowResponse.Status;
import com.gorson.springai.dto.response.RagAskResponse;
import com.gorson.springai.dto.result.ProjectSearchResult;
import com.gorson.springai.security.CurrentUser;
import com.gorson.springai.security.CurrentUserProvider;
import com.gorson.springai.security.ToolContextSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectWorkflowService {

    private final CurrentUserProvider currentUserProvider;
    private final ProjectQueryService projectQueryService;
    private final RagService ragService;

    public ProjectWorkflowResponse ask(ProjectWorkflowRequest request) {
        CurrentUser user = currentUserProvider.requireCurrentUser();
        ToolContextSupport.requireAuthority(user, "PROJECT_READ");

        ProjectSearchRequest search = new ProjectSearchRequest(
                request.projectKeyword().strip(),
                null,
                null,
                null
        );
        ProjectSearchResult projects =
                projectQueryService.searchProjects(search, user.tenantId());

        if (projects.total() == 0) {
            return new ProjectWorkflowResponse(
                    Status.PROJECT_NOT_FOUND, projects, null);
        }

        if (projects.total() > 1) {
            return new ProjectWorkflowResponse(
                    Status.SELECTION_REQUIRED, projects, null);
        }

        String projectId = projects.records().getFirst().projectId();
        RagAskResponse answer = ragService.ask(
                request.question(), projectId, 3, 0.0);

        return new ProjectWorkflowResponse(Status.ANSWERED, projects, answer);
    }
}