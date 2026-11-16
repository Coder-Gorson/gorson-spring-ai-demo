package com.gorson.springai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 文档入库
     */
    public void addDocuments(List<Document> documents) {
        vectorStore.add(documents);
    }

    /**
     * 相似度检索
     */
    public List<Document> search(String query, int topK) {

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        return vectorStore.similaritySearch(request);
    }

    /**
     * 根据项目ID进行向量检索
     */
    public List<Document> search(
            String query,
            int topK,
            String projectId,
            double threshold) {

        Filter.Expression filter = new FilterExpressionBuilder()
                .eq("projectId", projectId)
                .build();

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .filterExpression(filter)
                .build();

        return vectorStore.similaritySearch(request);
    }

    /**
     * 替换指定文件的全部向量数据
     */
    public void replaceSourceDocuments(
            String projectId,
            String source,
            List<Document> newDocuments) {

        if (newDocuments == null || newDocuments.isEmpty()) {
            throw new IllegalArgumentException("新文档不能为空");
        }

        // 1. 删除旧文件的全部Chunk
        deleteBySource(projectId, source);

        // 2. 写入新文件的全部Chunk
        vectorStore.add(newDocuments);
    }

    /**
     * 根据项目ID和文件名删除整份文件
     */
    public void deleteBySource(String projectId, String source) {

        FilterExpressionBuilder builder =
                new FilterExpressionBuilder();

        Filter.Expression expression = builder.and(
                builder.eq("projectId", projectId),
                builder.eq("source", source)
        ).build();

        vectorStore.delete(expression);
    }

    /**
     * 根据文档ID删除
     */
    public void deleteByIds(List<String> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("文档ID不能为空");
        }

        vectorStore.delete(ids);
    }
}