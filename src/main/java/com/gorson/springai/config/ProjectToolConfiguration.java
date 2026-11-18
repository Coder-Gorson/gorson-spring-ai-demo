package com.gorson.springai.config;

import com.gorson.springai.dto.tool.ProjectSearchRequest;
import com.gorson.springai.service.ProjectQueryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ProjectToolConfiguration {

    private static final Logger log =
            LoggerFactory.getLogger(ProjectToolConfiguration.class);

    @Bean
    public ToolCallback projectQueryToolCallback(
            ProjectQueryService projectQueryService) {

        ToolCallback callback = FunctionToolCallback.builder(
                        "searchProjects",
                        projectQueryService::searchProjects
                )

                .description("""
                        根据关键词、项目状态、创建日期范围和分页条件
                        查询企业投资项目列表。

                        可以根据项目名称、项目编号或企业名称搜索。
                        可以按项目状态筛选，也可以指定创建日期范围。
                        不提供分页参数时默认第一页，每页20条。
                        """)

                .inputType(ProjectSearchRequest.class)

                .build();

        // 打印实际生成的 JSON Schema，便于检查
        log.info(
                "Project search tool input schema: {}",
                callback.getToolDefinition().inputSchema()
        );

        return callback;
    }
}