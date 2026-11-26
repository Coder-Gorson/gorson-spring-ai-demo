package com.gorson.springai.controller;

import com.gorson.springai.dto.request.ChatRequest;
import com.gorson.springai.service.McpChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpChatController {

    private final McpChatService mcpChatService;

    @PostMapping("/chat")
    public String chat(@Valid @RequestBody ChatRequest request) {
        return mcpChatService.chat(request.question());
    }

}