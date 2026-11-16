package com.gorson.springai.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 单条文本向量化
     */
    public float[] embed(String text) {

        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("文本不能为空");
        }

        return embeddingModel.embed(text);
    }

    /**
     * 批量文本向量化
     */
    public List<float[]> embedBatch(List<String> texts) {

        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("文本集合不能为空");
        }

        if (texts.stream().anyMatch(text -> !StringUtils.hasText(text))) {
            throw new IllegalArgumentException("文本集合不能包含空文本");
        }

        return embeddingModel.embed(texts);
    }
}