package com.gorson.springai.controller;

import com.gorson.springai.service.InvestmentAdvisorChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/investment/advisor-chat")
public class InvestmentAdvisorChatController {

    private final InvestmentAdvisorChatService chatService;

    public InvestmentAdvisorChatController(
            InvestmentAdvisorChatService chatService) {
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

        return Map.of("status", "cleared");
    }

    public record ChatRequest(
            String conversationId,
            String projectName,
            String companyName,
            String industry,
            String question
    ) {
    }
}