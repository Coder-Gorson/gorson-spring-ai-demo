package com.gorson.springai.controller;

import com.gorson.springai.service.InvestmentAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/investment")
public class InvestmentPromptController {

    private final InvestmentAnalysisService analysisService;

    public InvestmentPromptController(
            InvestmentAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public Map<String, String> analyze(
            @RequestBody Map<String, String> request) {

        String answer = analysisService.analyze(
                request.get("projectName"),
                request.get("companyName"),
                request.get("industry"),
                request.get("question")
        );

        return Map.of("answer", answer);
    }
}