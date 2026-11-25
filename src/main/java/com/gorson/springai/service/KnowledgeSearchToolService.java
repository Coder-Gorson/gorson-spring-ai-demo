
package com.gorson.springai.service;

import com.gorson.springai.dto.tool.KnowledgeSearchRequest;
import com.gorson.springai.dto.tool.KnowledgeSearchResult;
import com.gorson.springai.security.CurrentUser;
import com.gorson.springai.security.ToolContextSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeSearchToolService {

    /**
     * 默认检索数量
     */
    private static final int DEFAULT_TOP_K = 5;

    private final VectorStoreService vectorStoreService;
    private final ProjectQueryService projectQueryService;

    /**
     * 知识库检索工具
     *
     * @param request     模型生成的检索参数
     * @param toolContext 应用传递的可信上下文
     * @return 知识库检索结果
     */
    public List<KnowledgeSearchResult> searchKnowledge(
            KnowledgeSearchRequest request,
            ToolContext toolContext) {

        // 1. 参数校验
        if (request == null || !StringUtils.hasText(request.query())) {
            throw new IllegalArgumentException("知识库检索问题不能为空");
        }
        if (!StringUtils.hasText(request.projectId())) {
            throw new IllegalArgumentException("目标项目编号不能为空");
        }

        // 2. 获取当前用户身份
        CurrentUser currentUser =
                ToolContextSupport.requireCurrentUser(toolContext);

        // 3. 权限校验
        ToolContextSupport.requireAuthority(
                currentUser,
                "KNOWLEDGE_READ"
        );
        String tenantId = currentUser.tenantId();
        String projectId = request.projectId();

        if (!projectQueryService.existsInTenant(tenantId, projectId)) {
            throw new SecurityException("项目不存在或无权访问");
        }

        log.info(
                "开始知识库检索，userId={}, tenantId={}, query={}",
                currentUser.userId(),
                currentUser.tenantId(),
                request.query()
        );

        // 4. 执行当前租户范围内的向量检索
//        List<Document> documents = vectorStoreService.search(
//                request.query(),
//                DEFAULT_TOP_K,
//                currentUser.tenantId()
//        );

        List<Document> documents = vectorStoreService.searchByTenantAndProject(
                request.query(),
                DEFAULT_TOP_K,
                tenantId,
                projectId,
                0.0
        );

        // 5. 转换检索结果
        List<KnowledgeSearchResult> results =
                convertResults(documents);

        log.info(
                "知识库检索完成，tenantId={}, 命中数量={}",
                currentUser.tenantId(),
                results.size()
        );

        return results;
    }

    /**
     * Document 转换为工具返回 DTO
     */
    private List<KnowledgeSearchResult> convertResults(
            List<Document> documents) {

        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return java.util.stream.IntStream
                .range(0, documents.size())
                .mapToObj(index -> {

                    Document document = documents.get(index);

                    return new KnowledgeSearchResult(
                            index + 1,
                            document.getId(),
                            document.getText(),
                            new LinkedHashMap<>(document.getMetadata())
                    );
                })
                .toList();
    }
}
