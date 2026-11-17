package com.gorson.springai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProjectQueryTools {

    /**
     * 根据项目ID查询项目基本信息
     */
    @Tool(
            name = "queryProjectBasicInfo",
            description = """
            根据项目ID查询企业投资项目的基本信息。
            当用户询问项目名称、所属企业、项目状态时调用。
            如果用户没有提供项目ID，应先向用户询问。
            """
    )
    public ProjectInfo queryProjectBasicInfo(
            @ToolParam(
                    description = "项目唯一编号，例如 project-001"
            )
            String projectId) {

        // 暂时使用模拟数据
        if ("project-001".equals(projectId)) {
            return new ProjectInfo(
                    true,
                    "project-001",
                    "华星科技新能源项目",
                    "华星科技有限公司",
                    "评估中"
            );
        }

        return new ProjectInfo(
                false,
                projectId,
                null,
                null,
                null
        );
    }

    /**
     * 工具返回的数据结构
     */
    public record ProjectInfo(
            boolean found,
            String projectId,
            String projectName,
            String companyName,
            String status
    ) {
    }
}