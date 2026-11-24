package com.gorson.springai.config;

import com.gorson.springai.dto.ProjectRecord;
import com.gorson.springai.dto.ProjectSearchResult;
import com.gorson.springai.dto.ProjectStatusStatistics;
import com.gorson.springai.dto.tool.KnowledgeSearchRequest;
import com.gorson.springai.dto.tool.KnowledgeSearchResult;
import com.gorson.springai.dto.tool.ProjectSearchRequest;
import com.gorson.springai.dto.tool.ProjectStatusStatisticsRequest;
import com.gorson.springai.service.KnowledgeSearchToolService;
import com.gorson.springai.service.ProjectQueryService;

import com.gorson.springai.service.ProjectToolService;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class ProjectToolConfiguration {

    /**
     * 项目查询工具
     */
    @Bean
    public ToolCallback projectQueryToolCallback(
            ProjectToolService projectToolService) {

        return FunctionToolCallback
                .<ProjectSearchRequest, ProjectSearchResult>builder(
                        "searchProjects",
                        projectToolService::searchProjects
                )
                .description("根据关键词查询当前用户有权限访问的项目")
                .inputType(ProjectSearchRequest.class)
                .build();
    }

    /**
     * 项目状态统计工具
     */
    @Bean
    public ToolCallback projectStatusStatisticsToolCallback(
            ProjectToolService projectToolService) {

        return FunctionToolCallback
                .<ProjectStatusStatisticsRequest, ProjectStatusStatistics>builder(
                        "countProjectsByStatus",
                        projectToolService::countProjectsByStatus
                )
                .description("统计当前用户有权限访问的项目状态")
                .inputType(ProjectStatusStatisticsRequest.class)
                .build();
    }

    /**
     * 创建具备多个工具能力的 ChatClient
     */
    @Bean
    public ChatClient projectChatClient(
            ChatClient.Builder builder,

            @Qualifier("projectQueryToolCallback")
            ToolCallback projectQueryToolCallback,

            @Qualifier("projectStatusStatisticsToolCallback")
            ToolCallback projectStatusStatisticsToolCallback,

            @Qualifier("knowledgeSearchToolCallback")
            ToolCallback knowledgeSearchToolCallback) {

        return builder
                .defaultTools(
                        projectQueryToolCallback,
                        projectStatusStatisticsToolCallback,
                        knowledgeSearchToolCallback
                )
                .build();
    }

    @Bean
    public ToolCallback knowledgeSearchToolCallback(
            KnowledgeSearchToolService knowledgeSearchToolService) {

        return FunctionToolCallback
                .<KnowledgeSearchRequest, List<KnowledgeSearchResult>>builder(
                        "searchKnowledgeBase",
                        knowledgeSearchToolService::searchKnowledge
                )
                .description("""
                    搜索当前用户所属租户的企业投资知识库。

                    用于查询企业经营、财务数据、
                    行业环境、项目背景及风险信息。

                    只能返回当前用户所属租户有权访问的资料。
                    """)
                .inputType(KnowledgeSearchRequest.class)
                .build();
    }
}