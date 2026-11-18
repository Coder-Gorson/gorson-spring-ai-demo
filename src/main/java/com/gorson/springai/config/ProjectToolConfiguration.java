package com.gorson.springai.config;

import com.gorson.springai.service.ProjectQueryService;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ProjectToolConfiguration {

    @Bean
    public ToolCallback projectQueryToolCallback(
            ProjectQueryService projectQueryService) {

        return FunctionToolCallback.builder(
                        "queryProjectBasicInfo",
                        projectQueryService::queryProjectBasicInfo
                )
                .description("""
                        根据项目ID查询企业投资项目的基本信息。
                        当用户询问项目名称、所属企业、
                        项目状态时调用。
                        """)
                .inputType(
                        ProjectQueryService.ProjectQueryRequest.class
                )
                .build();
    }
}