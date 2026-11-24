
package com.gorson.springai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final VectorStore vectorStore;

    /**
     * 批量添加文档
     */
    public void addDocuments(List<Document> documents) {

        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("文档列表不能为空");
        }

        vectorStore.add(documents);

        log.info("文档写入成功，数量：{}", documents.size());
    }

    /**
     * 普通向量检索
     * 供原有 RAG 业务使用
     */
    public List<Document> search(String query, int topK) {

        validateSearchParams(query, topK);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        List<Document> documents =
                vectorStore.similaritySearch(request);

        log.info(
                "向量检索完成，query={}, topK={}, 命中数量={}",
                query,
                topK,
                documents.size()
        );

        return documents;
    }

    /**
     * 按租户隔离的向量检索
     * 供带 ToolContext 的知识库工具使用
     */
    public List<Document> search(
            String query,
            int topK,
            String tenantId) {

        validateSearchParams(query, topK);

        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("租户ID不能为空");
        }

        // 构建租户过滤表达式
        Filter.Expression tenantFilter =
                new FilterExpressionBuilder()
                        .eq("tenantId", tenantId)
                        .build();

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression(tenantFilter)
                .build();

        List<Document> documents =
                vectorStore.similaritySearch(request);

        log.info(
                "租户向量检索完成，tenantId={}, 命中数量={}",
                tenantId,
                documents.size()
        );

        return documents;
    }

    /**
     * 按租户和项目进行向量检索
     *
     * 供企业知识库 Tool 和项目级 RAG 使用
     *
     * @param query               用户检索问题
     * @param topK                最大返回数量
     * @param tenantId            当前用户所属租户
     * @param projectId           当前用户有权访问的项目
     * @param similarityThreshold 相似度阈值
     * @return 符合条件的知识文档
     */
    public List<Document> searchByTenantAndProject(
            String query,
            int topK,
            String tenantId,
            String projectId,
            double similarityThreshold) {

        // 1. 检索参数校验
        validateSearchParams(query, topK);

        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("租户ID不能为空");
        }

        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("项目ID不能为空");
        }

        if (!Double.isFinite(similarityThreshold)
                || similarityThreshold < 0.0
                || similarityThreshold > 1.0) {
            throw new IllegalArgumentException(
                    "相似度阈值必须在 0.0 到 1.0 之间"
            );
        }

        // 2. 构建租户 + 项目过滤条件
        FilterExpressionBuilder builder =
                new FilterExpressionBuilder();

        Filter.Expression filterExpression = builder.and(
                builder.eq("tenantId", tenantId),
                builder.eq("projectId", projectId)
        ).build();

        // 3. 构建向量检索请求
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression(filterExpression)
                .build();

        // 4. 执行向量检索
        List<Document> documents =
                vectorStore.similaritySearch(request);

        log.info(
                "项目知识库检索完成，tenantId={}, projectId={}, topK={}, 命中数量={}",
                tenantId,
                projectId,
                topK,
                documents.size()
        );

        return documents;
    }

    /**
     * 按租户、项目和 Chunk ID 删除文档
     */
    public void deleteByIds(
            List<String> chunkIds,
            String tenantId,
            String projectId) {

        // 1. 参数校验
        validateScope(tenantId, projectId);

        if (chunkIds == null || chunkIds.isEmpty()) {
            throw new IllegalArgumentException("Chunk ID 列表不能为空");
        }

        if (chunkIds.stream().anyMatch(id -> !StringUtils.hasText(id))) {
            throw new IllegalArgumentException("Chunk ID 不能为空");
        }

        List<String> distinctIds = chunkIds.stream()
                .distinct()
                .toList();

        // 2. 构建租户 + 项目 + Chunk ID 过滤条件
        FilterExpressionBuilder builder =
                new FilterExpressionBuilder();

        Filter.Expression filterExpression = builder.and(
                builder.and(
                        builder.eq("tenantId", tenantId),
                        builder.eq("projectId", projectId)
                ),
                builder.in("chunkId", distinctIds.toArray())
        ).build();

        // 3. 执行删除
        vectorStore.delete(filterExpression);

        log.info(
                "Chunk 删除完成，tenantId={}, projectId={}, 请求删除数量={}",
                tenantId,
                projectId,
                distinctIds.size()
        );
    }

    /**
     * 按租户、项目和文件来源删除全部 Chunk
     */
    public void deleteBySource(
            String tenantId,
            String projectId,
            String source) {

        // 1. 参数校验
        validateScope(tenantId, projectId);

        if (!StringUtils.hasText(source)) {
            throw new IllegalArgumentException("文件来源不能为空");
        }

        // 2. 构建租户 + 项目 + source 过滤条件
        FilterExpressionBuilder builder =
                new FilterExpressionBuilder();

        Filter.Expression filterExpression = builder.and(
                builder.and(
                        builder.eq("tenantId", tenantId),
                        builder.eq("projectId", projectId)
                ),
                builder.eq("source", source)
        ).build();

        // 3. 执行删除
        vectorStore.delete(filterExpression);

        log.info(
                "文件向量删除完成，tenantId={}, projectId={}, source={}",
                tenantId,
                projectId,
                source
        );
    }

    /**
     * 检索参数校验
     */
    private void validateSearchParams(String query, int topK) {

        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("检索内容不能为空");
        }

        if (topK <= 0) {
            throw new IllegalArgumentException("topK必须大于0");
        }
    }

    /**
     * 校验租户和项目范围
     */
    private void validateScope(
            String tenantId,
            String projectId) {

        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("租户ID不能为空");
        }

        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
    }
}
