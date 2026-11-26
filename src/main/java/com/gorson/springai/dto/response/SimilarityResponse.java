package com.gorson.springai.dto.response;

import java.util.List;

public record SimilarityResponse(
        int inputCount,
        int comparisonCount,
        List<PairResult> comparisons
) {

    public record PairResult(
            int indexA,
            int indexB,
            String textA,
            String textB,
            double cosineSimilarity
    ) {
    }
}