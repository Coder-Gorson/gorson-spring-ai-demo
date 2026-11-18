package com.gorson.springai.service;

import org.springframework.stereotype.Service;

@Service
public class ProjectQueryService {

    public ProjectInfo queryProjectBasicInfo(
            ProjectQueryRequest request) {

        String projectId = request.projectId();

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
     * 工具输入参数
     */
    public record ProjectQueryRequest(
            String projectId
    ) {
    }

    /**
     * 工具返回结果
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