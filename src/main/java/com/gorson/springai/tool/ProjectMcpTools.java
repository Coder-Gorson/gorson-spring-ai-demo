package com.gorson.springai.tool;

import com.gorson.springai.dto.ProjectSearchResult;
import com.gorson.springai.dto.tool.ProjectSearchRequest;
import com.gorson.springai.security.CurrentUser;
import com.gorson.springai.security.CurrentUserProvider;
import com.gorson.springai.security.ToolContextSupport;
import com.gorson.springai.service.ProjectQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class ProjectMcpTools {

    private final CurrentUserProvider currentUserProvider;
    private final ProjectQueryService projectQueryService;

    @McpTool(
            name = "searchProjects",
            description = "按关键词、状态、创建日期和分页条件查询当前租户的项目"
    )
    public ProjectSearchResult searchProjects(
            @McpToolParam(description = "项目查询条件", required = true)
            ProjectSearchRequest request) {

        CurrentUser user = currentUserProvider.requireCurrentUser();
        ToolContextSupport.requireAuthority(user, "PROJECT_READ");

        return projectQueryService.searchProjects(request, user.tenantId());
    }
}