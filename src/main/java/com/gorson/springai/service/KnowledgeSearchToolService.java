package com.gorson.springai.service;

import com.gorson.springai.dto.tool.KnowledgeSearchRequest;
import com.gorson.springai.dto.tool.KnowledgeSearchResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeSearchToolService {

    private final VectorStoreService vectorStoreService;

    /**
     * 搜索企业知识库
     */
    public List<KnowledgeSearchResult> searchKnowledge(
            KnowledgeSearchRequest request) {

        if (request == null || !StringUtils.hasText(request.query())) {
            throw new IllegalArgumentException("知识库检索问题不能为空");
        }

        log.info("开始执行知识库检索，query={}", request.query());

        // 复用已有 VectorStoreService
        List<Document> documents =
                vectorStoreService.search(request.query(), 5);

        log.info("知识库检索完成，命中数量={}", documents.size());

        return java.util.stream.IntStream.range(0, documents.size())
                .mapToObj(index -> {

                    Document document = documents.get(index);

                    return new KnowledgeSearchResult(
                            index + 1,
                            document.getId(),
                            document.getText(),
                            document.getMetadata()
                    );
                })
                .toList();
    }
}