package com.gorson.springai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentSplitService {

    private final TokenTextSplitter textSplitter;

    public DocumentSplitService() {

        this.textSplitter = TokenTextSplitter.builder()
                // 每个 Chunk 的目标 Token 数量
                .withChunkSize(500)

                // 寻找切分标点前的最小字符长度
                .withMinChunkSizeChars(200)

                // 过滤过短的文本片段
                .withMinChunkLengthToEmbed(10)

                // 最大 Chunk 数量
                .withMaxNumChunks(5000)

                // 保留分隔符
                .withKeepSeparator(true)

                // 适配中文文档标点
                .withPunctuationMarks(
                        List.of('.', '?', '!', '\n',
                                '。', '？', '！', '；')
                )
                .build();
    }

    /**
     * 将原始 Document 切分为多个 Chunk
     */
    public List<Document> split(List<Document> documents) {

        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return textSplitter.apply(documents);
    }
}