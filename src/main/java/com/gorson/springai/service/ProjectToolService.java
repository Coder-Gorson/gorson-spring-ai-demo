package com.gorson.springai.service;

import com.gorson.springai.dto.result.ProjectSearchResult;
import com.gorson.springai.dto.ProjectStatusStatistics;
import com.gorson.springai.dto.request.ProjectSearchRequest;
import com.gorson.springai.dto.request.ProjectStatusStatisticsRequest;
import com.gorson.springai.security.CurrentUser;
import com.gorson.springai.security.ToolContextSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectToolService {

    private final ProjectQueryService projectQueryService;

    public ProjectSearchResult searchProjects(
            ProjectSearchRequest request,
            ToolContext toolContext) {

        CurrentUser user =
                ToolContextSupport.requireCurrentUser(toolContext);

        ToolContextSupport.requireAuthority(
                user,
                "PROJECT_READ"
        );

        log.info(
                "用户查询项目，userId={}, tenantId={}",
                user.userId(),
                user.tenantId()
        );

        return projectQueryService.searchProjects(
                request,
                user.tenantId()
        );
    }

    public ProjectStatusStatistics countProjectsByStatus(
            ProjectStatusStatisticsRequest request,
            ToolContext toolContext) {

        CurrentUser user =
                ToolContextSupport.requireCurrentUser(toolContext);

        ToolContextSupport.requireAuthority(
                user,
                "PROJECT_READ"
        );

        return projectQueryService.countProjectsByStatus(
                request,
                user.tenantId()
        );
    }

}