package com.gorson.springai.controller;

import com.gorson.springai.dto.EmbeddingTestRequest;
import com.gorson.springai.dto.EmbeddingTestResponse;
import com.gorson.springai.service.EmbeddingService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/rag/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    @Value("${spring.ai.openai.embedding.model:BAAI/bge-m3}")
    private String modelName;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * 批量文本向量化测试
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmbeddingTestResponse embed(
            @RequestBody EmbeddingTestRequest request) {

        // 1. 请求参数校验
        if (request == null ||
                request.texts() == null ||
                request.texts().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "文本集合不能为空"
            );
        }

        if (request.texts().size() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "单次最多处理20条文本"
            );
        }

        if (request.texts().stream()
                .anyMatch(text -> !StringUtils.hasText(text))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "文本不能包含空内容"
            );
        }

        // 2. 调用 EmbeddingService
        List<float[]> vectors =
                embeddingService.embedBatch(request.texts());

        if (vectors.size() != request.texts().size()) {
            throw new IllegalStateException(
                    "输入文本数量与返回向量数量不一致"
            );
        }

        // 3. 构建响应
        List<EmbeddingTestResponse.VectorItem> items =
                IntStream.range(0, vectors.size())
                        .mapToObj(index -> {

                            float[] vector = vectors.get(index);

                            // 只取前5个维度用于展示
                            List<Float> previewValues =
                                    IntStream.range(
                                                    0,
                                                    Math.min(5, vector.length)
                                            )
                                            .mapToObj(i -> vector[i])
                                            .toList();

                            String text = request.texts().get(index);

                            String textPreview = text.length() > 100
                                    ? text.substring(0, 100) + "..."
                                    : text;

                            return new EmbeddingTestResponse.VectorItem(
                                    index,
                                    textPreview,
                                    vector.length,
                                    previewValues
                            );
                        })
                        .toList();

        // 4. 返回结果
        return new EmbeddingTestResponse(
                modelName,
                request.texts().size(),
                vectors.size(),
                items
        );
    }
}