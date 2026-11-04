package com.gorson.springai.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestmentAnalysisService {

    private final ChatModel chatModel;

    private final InvestmentPromptService investmentPromptService;

    public InvestmentAnalysisService(
            ChatModel chatModel,
            InvestmentPromptService investmentPromptService) {
        this.chatModel = chatModel;
        this.investmentPromptService = investmentPromptService;
    }

    public String analyze(
            String projectName,
            String companyName,
            String industry,
            String question) {

        // 1. 动态构建 SystemMessage
        Message systemMessage =
                investmentPromptService.buildSystemMessage(
                        projectName,
                        companyName,
                        industry
                );

        // 2. 构建 UserMessage
        Message userMessage = new UserMessage(question);

        // 3. 组合消息
        List<Message> messages = List.of(
                systemMessage,
                userMessage
        );

        // 4. 设置模型参数
        DeepSeekChatOptions options =
                DeepSeekChatOptions.builder()
                        .temperature(0.2)
                        .maxTokens(1024)
                        .build();

        // 5. 构建 Prompt
        Prompt prompt = new Prompt(messages, options);

        // 6. 调用 DeepSeek
        ChatResponse response = chatModel.call(prompt);

        // 7. 获取模型回答
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {
            throw new IllegalStateException("模型未返回有效结果");
        }

        return response.getResult()
                .getOutput()
                .getText();
    }
}