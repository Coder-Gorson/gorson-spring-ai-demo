package com.gorson.springai.controller;

import com.gorson.springai.service.InvestmentChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/investment/chat")
public class InvestmentChatController {

    private final InvestmentChatService chatService;

    public InvestmentChatController(
            InvestmentChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, String> chat(
            @RequestBody ChatRequest request) {

        String answer = chatService.chat(
                request.conversationId(),
                request.projectName(),
                request.companyName(),
                request.industry(),
                request.question()
        );

        return Map.of(
                "conversationId", request.conversationId(),
                "answer", answer
        );
    }

    @DeleteMapping("/{conversationId}")
    public Map<String, String> clear(
            @PathVariable String conversationId) {

        chatService.clear(conversationId);

        return Map.of(
                "conversationId", conversationId,
                "status", "cleared"
        );
    }

    public record ChatRequest(
            String conversationId,
            String projectName,
            String companyName,
            String industry,
            String question) {
    }
}