package com.gorson.springai.controller;

import com.gorson.springai.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, String> chat(
            @RequestBody Map<String, String> request) {

        String message = request.get("message");

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message不能为空");
        }

        String answer = chatService.chat(message);

        return Map.of("answer", answer);
    }
}