package com.gorson.springai.service;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InvestmentChatService {

    private final ChatMemory chatMemory;

    private final ChatModel chatModel;

    private final InvestmentPromptService investmentPromptService;

    public InvestmentChatService(
            ChatMemory chatMemory,
            ChatModel chatModel,
            InvestmentPromptService investmentPromptService) {

        this.chatMemory = chatMemory;
        this.chatModel = chatModel;
        this.investmentPromptService = investmentPromptService;
    }

    public String chat(
            String conversationId,
            String projectName,
            String companyName,
            String industry,
            String question) {

        // 1. 查询当前会话历史
        List<Message> history = new ArrayList<>(
                chatMemory.get(conversationId)
        );

        // 2. 判断是否需要初始化 SystemMessage
        Message systemMessage = null;

        boolean hasSystemMessage = history.stream()
                .anyMatch(SystemMessage.class::isInstance);

        if (!hasSystemMessage) {

            systemMessage =
                    investmentPromptService.buildSystemMessage(
                            projectName,
                            companyName,
                            industry
                    );

            history.add(0, systemMessage);
        }

        // 3. 添加本轮用户消息
        UserMessage userMessage = new UserMessage(question);
        history.add(userMessage);

        // 4. 构建 Prompt
        Prompt prompt = new Prompt(history);

        // 5. 调用 DeepSeek
        ChatResponse response = chatModel.call(prompt);

        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput() == null) {
            throw new IllegalStateException("DeepSeek 未返回有效结果");
        }

        AssistantMessage assistantMessage =
                response.getResult().getOutput();

        if (assistantMessage.getText() == null) {
            throw new IllegalStateException("模型返回内容为空");
        }

        // 6. 保存本次成功完成的对话
        List<Message> newMessages = new ArrayList<>();

        // 只有首次初始化时才保存 SystemMessage
        if (systemMessage != null) {
            newMessages.add(systemMessage);
        }

        newMessages.add(userMessage);
        newMessages.add(assistantMessage);

        chatMemory.add(conversationId, newMessages);

        // 7. 返回模型回答
        return assistantMessage.getText();
    }

    /**
     * 清理指定会话
     */
    public void clear(String conversationId) {
        chatMemory.clear(conversationId);
    }
}