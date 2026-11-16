package com.gorson.springai.util;

public final class VectorSimilarity {

    private VectorSimilarity() {
    }

    public static double cosine(float[] a, float[] b) {

        if (a == null || b == null
                || a.length == 0
                || a.length != b.length) {
            throw new IllegalArgumentException("向量不能为空且维度必须一致");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {

            dotProduct += a[i] * b[i];

            normA += a[i] * a[i];

            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            throw new IllegalArgumentException("零向量无法计算余弦相似度");
        }

        return dotProduct /
                (Math.sqrt(normA) * Math.sqrt(normB));
    }
}