package com.gorson.springai.service;

import com.gorson.springai.util.VectorSimilarity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TextSimilarityService {

    private final EmbeddingService embeddingService;

    public TextSimilarityService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * 批量文本两两相似度比较
     */
    public List<PairSimilarity> compare(List<String> texts) {

        // 1. 批量生成文本向量
        List<float[]> vectors = embeddingService.embedBatch(texts);

        List<PairSimilarity> results = new ArrayList<>();

        // 2. 两两计算余弦相似度
        for (int i = 0; i < vectors.size(); i++) {

            for (int j = i + 1; j < vectors.size(); j++) {

                double similarity = VectorSimilarity.cosine(
                        vectors.get(i),
                        vectors.get(j)
                );

                results.add(new PairSimilarity(
                        i,
                        j,
                        texts.get(i),
                        texts.get(j),
                        similarity
                ));
            }
        }

        // 3. 按相似度降序排列
        results.sort(
                Comparator.comparingDouble(
                        PairSimilarity::cosineSimilarity
                ).reversed()
        );

        return results;
    }

    public record PairSimilarity(
            int indexA,
            int indexB,
            String textA,
            String textB,
            double cosineSimilarity
    ) {
    }
}