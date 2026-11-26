package com.gorson.springai.controller;

import com.gorson.springai.dto.request.SimilarityRequest;
import com.gorson.springai.dto.response.SimilarityResponse;
import com.gorson.springai.service.TextSimilarityService;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/rag/similarity")
public class SimilarityController {

    private final TextSimilarityService similarityService;

    public SimilarityController(
            TextSimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    @PostMapping
    public SimilarityResponse compare(
            @RequestBody SimilarityRequest request) {

        // 1. 参数校验
        if (request == null
                || request.texts() == null
                || request.texts().size() < 2) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "至少需要提供两条文本"
            );
        }

        if (request.texts().size() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "单次最多比较20条文本"
            );
        }

        if (request.texts().stream()
                .anyMatch(text -> !StringUtils.hasText(text))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "文本不能为空"
            );
        }

        // 2. 执行相似度计算
        List<TextSimilarityService.PairSimilarity> results =
                similarityService.compare(request.texts());

        // 3. 转换响应
        List<SimilarityResponse.PairResult> comparisons =
                results.stream()
                        .map(item -> new SimilarityResponse.PairResult(
                                item.indexA(),
                                item.indexB(),
                                item.textA(),
                                item.textB(),
                                item.cosineSimilarity()
                        ))
                        .toList();

        return new SimilarityResponse(
                request.texts().size(),
                comparisons.size(),
                comparisons
        );
    }
}