package com.gorson.springai.controller;

import com.gorson.springai.dto.RagAskRequest;
import com.gorson.springai.dto.RagAskResponse;
import com.gorson.springai.service.RagService;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * RAG 问答接口
     */
    @PostMapping("/ask")
    public RagAskResponse ask(@RequestBody RagAskRequest request) {

        if (request == null
                || !StringUtils.hasText(request.question())
                || !StringUtils.hasText(request.projectId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "问题和项目ID不能为空"
            );
        }

        int topK = request.topK() == null
                ? 3
                : request.topK();

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

        return ragService.ask(
                request.question(),
                request.projectId(),
                topK,
                threshold
        );
    }
}