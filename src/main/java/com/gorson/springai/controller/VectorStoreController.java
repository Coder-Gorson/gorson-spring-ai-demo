package com.gorson.springai.controller;

import com.gorson.springai.dto.*;
import com.gorson.springai.security.CurrentUser;
import com.gorson.springai.security.CurrentUserProvider;
import com.gorson.springai.security.ToolContextSupport;
import com.gorson.springai.service.VectorStoreService;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rag/vector-store")
@RequiredArgsConstructor
public class VectorStoreController {

    private final VectorStoreService vectorStoreService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * 批量文档入库
     */
    @PostMapping("/documents")
    public VectorStoreAddResponse addDocuments(
            @RequestBody VectorStoreAddRequest request) {

        if (request == null
                || request.documents() == null
                || request.documents().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "文档集合不能为空"
            );
        }

        if (request.documents().size() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "单次最多添加100条文档"
            );
        }

        // 获取当前用户
        CurrentUser currentUser =
                currentUserProvider.requireCurrentUser();

        // 校验知识库写入权限
        ToolContextSupport.requireAuthority(
                currentUser,
                "KNOWLEDGE_WRITE"
        );

        List<Document> documents = request.documents().stream()
                .map(item -> {

                    if (item == null
                            || !StringUtils.hasText(item.id())
                            || !StringUtils.hasText(item.text())
                            || !StringUtils.hasText(item.source())
                            || !StringUtils.hasText(item.projectId())
                            || item.page() == null
                            || item.page() < 1) {

                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "文档ID、文本、来源和页码必须有效"
                        );
                    }

                    String sourceKey = generateSourceKey(item.source());

                    Map<String, Object> metadata = Map.of(
                            "tenantId", currentUser.tenantId(),
                            "chunkId", item.id(),
                            "projectId", item.projectId(),
                            "source", item.source(),
                            "sourceVersion", UUID.randomUUID().toString(),
                            "sourceKey", sourceKey,
                            "page", item.page()
                    );

                    return new Document(
                            item.id(),
                            item.text(),
                            metadata
                    );
                })
                .toList();

        vectorStoreService.addDocuments(documents);

        return new VectorStoreAddResponse(
                documents.size(),
                documents.stream()
                        .map(Document::getId)
                        .toList()
        );
    }

    /**
     * 向量相似度检索
     */
    @PostMapping("/search")
    public VectorStoreSearchResponse search(
            @RequestBody VectorStoreSearchRequest request) {

        if (request == null || !StringUtils.hasText(request.query())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "查询内容不能为空"
            );
        }

        int topK = request.topK() == null ? 3 : request.topK();

        double threshold = request.similarityThreshold() == null
                ? 0.0
                : request.similarityThreshold();

        if (topK < 1 || topK > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "topK 必须在1到20之间"
            );
        }

        if (threshold < 0 || threshold > 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "similarityThreshold 必须在0到1之间"
            );
        }

        if (!StringUtils.hasText(request.projectId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "projectId 不能为空"
            );
        }

        CurrentUser currentUser =
                currentUserProvider.requireCurrentUser();

        ToolContextSupport.requireAuthority(
                currentUser,
                "KNOWLEDGE_READ"
        );

        List<Document> documents =
                vectorStoreService.searchByTenantAndProject(
                        request.query(),
                        topK,
                        currentUser.tenantId(),
                        request.projectId(),
                        threshold
                );

        List<VectorStoreSearchResponse.SearchResult> results =
                documents.stream()
                        .map(document ->
                                new VectorStoreSearchResponse.SearchResult(
                                        document.getId(),
                                        document.getText(),
                                        document.getMetadata(),
                                        document.getScore()
                                )
                        )
                        .toList();

        return new VectorStoreSearchResponse(
                request.query(),
                results.size(),
                results
        );
    }

    @DeleteMapping("/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable String id, @RequestParam String projectId) {

        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "文档ID不能为空"
            );
        }

        if (!StringUtils.hasText(projectId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "projectId不能为空"
            );
        }

        CurrentUser currentUser =
                currentUserProvider.requireCurrentUser();

        vectorStoreService.deleteByIds(
                List.of(id),
                currentUser.tenantId(),
                projectId
        );
    }

    @DeleteMapping("/sources")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBySource(
            @RequestParam String projectId,
            @RequestParam String source) {

        if (!StringUtils.hasText(projectId)
                || !StringUtils.hasText(source)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "项目ID和文件名称不能为空"
            );
        }

        CurrentUser currentUser =
                currentUserProvider.requireCurrentUser();

        vectorStoreService.deleteBySource(
                currentUser.tenantId(),
                projectId,
                source
        );
    }

    /**
     * 替换指定文件的全部向量数据
     */
    @PutMapping("/sources")
    public VectorStoreAddResponse replaceSource(
            @RequestBody VectorStoreReplaceRequest request) {

        if (request == null
                || !StringUtils.hasText(request.projectId())
                || !StringUtils.hasText(request.source())
                || request.documents() == null
                || request.documents().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "项目ID、文件名称和文档内容不能为空"
            );
        }

        CurrentUser currentUser =
                currentUserProvider.requireCurrentUser();

        ToolContextSupport.requireAuthority(
                currentUser,
                "KNOWLEDGE_WRITE"
        );

        List<Document> documents = request.documents().stream()
                .map(item -> {

                    if (item == null
                            || !StringUtils.hasText(item.id())
                            || !StringUtils.hasText(item.text())
                            || item.page() == null
                            || item.page() < 1) {

                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "文档ID、文本和页码必须有效"
                        );
                    }

                    Map<String, Object> metadata = Map.of(
                            "projectId", request.projectId(),
                            "source", request.source(),
                            "page", item.page()
                    );

                    return new Document(
                            item.id(),
                            item.text(),
                            metadata
                    );
                })
                .toList();

        vectorStoreService.replaceSourceDocuments(
                currentUser.tenantId(),
                request.projectId(),
                request.source(),
                documents
        );

        return new VectorStoreAddResponse(
                documents.size(),
                documents.stream()
                        .map(Document::getId)
                        .toList()
        );
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
}