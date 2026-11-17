package com.gorson.springai.controller;

import com.gorson.springai.service.ToolCallingService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tool")
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    public ToolCallingController(
            ToolCallingService toolCallingService) {

        this.toolCallingService = toolCallingService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {

        return toolCallingService.chat(request.question());
    }

    public record ChatRequest(String question) {
    }
}