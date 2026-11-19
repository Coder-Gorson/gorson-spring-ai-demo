package com.gorson.springai.config;

import com.gorson.springai.dto.tool.KnowledgeSearchRequest;
import com.gorson.springai.dto.tool.ProjectSearchRequest;
import com.gorson.springai.dto.tool.ProjectStatusStatisticsRequest;
import com.gorson.springai.service.KnowledgeSearchToolService;
import com.gorson.springai.service.ProjectQueryService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class ProjectToolConfiguration {

    private final ProjectQueryService projectQueryService;

    /**
     * 项目查询工具
     */
    @Bean
    public ToolCallback projectQueryToolCallback() {

        return FunctionToolCallback.builder(
                        "searchProjects",
                        projectQueryService::searchProjects
                )
                .description("""
                        根据项目名称、项目编号或企业名称关键词，
                        查询企业投资项目列表。

                        返回项目编号、项目名称、企业名称、
                        项目状态及相关项目信息。
                        """)
                .inputType(ProjectSearchRequest.class)
                .build();
    }

    /**
     * 项目状态统计工具
     */
    @Bean
    public ToolCallback projectStatusStatisticsToolCallback() {

        return FunctionToolCallback.builder(
                        "countProjectsByStatus",
                        projectQueryService::countProjectsByStatus
                )
                .description("""
                        根据项目编号列表统计项目状态分布。

                        项目编号必须来自 searchProjects 工具的实际返回结果。

                        返回项目总数，以及待评估、进行中、
                        已完成、已取消等状态的数量。
                        """)
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

        return FunctionToolCallback.builder(
                        "searchKnowledgeBase",
                        knowledgeSearchToolService::searchKnowledge
                )
                .description("""
                    搜索企业投资评价知识库。

                    用于查询企业经营情况、财务数据、
                    行业环境、项目背景、风险信息等资料。

                    当用户要求基于企业资料进行分析时，
                    应调用此工具获取知识库中的相关证据。

                    返回内容包括文档编号、文本内容和元数据。
                    不允许编造检索结果。
                    """)
                .inputType(KnowledgeSearchRequest.class)
                .build();
    }
}