package com.gorson.springai.config;

import com.gorson.springai.dto.tool.ProjectSearchRequest;
import com.gorson.springai.dto.tool.ProjectStatusStatisticsRequest;
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
            ToolCallback projectStatusStatisticsToolCallback) {

        return builder
                .defaultTools(
                        projectQueryToolCallback,
                        projectStatusStatisticsToolCallback
                )
                .build();
    }
}