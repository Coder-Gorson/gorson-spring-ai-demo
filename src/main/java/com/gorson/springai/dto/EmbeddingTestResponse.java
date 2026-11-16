package com.gorson.springai.dto;

import java.util.List;

public record EmbeddingTestResponse(

        String model,

        int inputCount,

        int vectorCount,

        List<VectorItem> vectors

) {

    public record VectorItem(

            int index,

            String textPreview,

            int dimensions,

            List<Float> firstFiveValues

    ) {
    }
}