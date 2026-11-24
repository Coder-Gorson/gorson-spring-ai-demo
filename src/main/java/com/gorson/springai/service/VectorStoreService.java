
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
     * 替换指定租户、项目下的文件向量数据。
     *
     * 采用新版本先入库、旧版本后清理的方式。
     */
    public void replaceSourceDocuments(
            String tenantId,
            String projectId,
            String source,
            List<Document> documents) {

        // 1. 参数校验
        validateScope(tenantId, projectId);

        if (!StringUtils.hasText(source)) {
            throw new IllegalArgumentException("文件来源不能为空");
        }

        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("替换文档不能为空");
        }

        // 2. 生成本次替换的版本号
        String newVersion = UUID.randomUUID().toString();
        String sourceKey = generateSourceKey(source);

        // 3. 构建新版本 Document
        List<Document> newDocuments = documents.stream()
                .map(document -> {

                    if (document == null
                            || !StringUtils.hasText(document.getId())
                            || !StringUtils.hasText(document.getText())) {
                        throw new IllegalArgumentException(
                                "Document ID 和文本不能为空"
                        );
                    }

                    Map<String, Object> metadata =
                            new HashMap<>(document.getMetadata());

                    // 服务端统一确定文档归属
                    metadata.put("tenantId", tenantId);
                    metadata.put("projectId", projectId);
                    metadata.put("source", source);
                    metadata.put("sourceKey", sourceKey);
                    metadata.put("chunkId", document.getId());
                    metadata.put("sourceVersion", newVersion);

                    return new Document(
                            document.getId(),
                            document.getText(),
                            metadata
                    );
                })
                .toList();

        // 4. 新版本先入库
        try {
            vectorStore.add(newDocuments);
        } catch (RuntimeException e) {

            // 如果新版本发生部分写入，尝试清理本次版本
            try {
                FilterExpressionBuilder builder =
                        new FilterExpressionBuilder();

                Filter.Expression cleanupFilter = builder.and(
                        builder.and(
                                builder.eq("tenantId", tenantId),
                                builder.eq("projectId", projectId)
                        ),
                        builder.and(
                                builder.eq("sourceKey", sourceKey),
                                builder.eq("sourceVersion", newVersion)
                        )
                ).build();

                vectorStore.delete(cleanupFilter);

            } catch (RuntimeException cleanupException) {
                e.addSuppressed(cleanupException);
            }

            throw new IllegalStateException(
                    "新版本入库失败，旧版本保留",
                    e
            );
        }

        // 5. 新版本成功后，删除旧版本
        FilterExpressionBuilder builder =
                new FilterExpressionBuilder();

        Filter.Expression oldVersionFilter = builder.and(
                builder.and(
                        builder.eq("tenantId", tenantId),
                        builder.eq("projectId", projectId)
                ),
                builder.and(
                        builder.eq("sourceKey", sourceKey),
                        builder.ne("sourceVersion", newVersion)
                )
        ).build();

        try {
            vectorStore.delete(oldVersionFilter);

        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "新版本已入库，但旧版本清理失败，可能存在多个版本",
                    e
            );
        }

        log.info(
                "文件替换完成，tenantId={}, projectId={}, source={}, version={}, chunkCount={}",
                tenantId,
                projectId,
                source,
                newVersion,
                newDocuments.size()
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

    private String generateSourceKey(String source) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("生成文件标识失败", e);
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
