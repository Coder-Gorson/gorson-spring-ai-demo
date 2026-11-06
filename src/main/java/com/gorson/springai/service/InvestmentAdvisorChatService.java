package com.gorson.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class InvestmentAdvisorChatService {

    private final ChatClient chatClient;

    private final InvestmentPromptService promptService;

    private final ChatMemory chatMemory;

    public InvestmentAdvisorChatService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            InvestmentPromptService promptService) {

        this.chatMemory = chatMemory;
        this.promptService = promptService;

        // 构建带记忆管理能力的 ChatClient
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    public String chat(
            String conversationId,
            String projectName,
            String companyName,
            String industry,
            String question) {

        // 1. 构建动态 SystemMessage
        Message systemMessage =
                promptService.buildSystemMessage(
                        projectName,
                        companyName,
                        industry
                );

        // 2. 使用 ChatClient 发起请求
        return chatClient.prompt()

                // 传入系统消息和用户问题
                .messages(
                        systemMessage,
                        new UserMessage(question)
                )

                // 每次调用都指定会话 ID
                .advisors(advisorSpec ->
                        advisorSpec.param(
                                ChatMemory.CONVERSATION_ID,
                                conversationId
                        )
                )

                // 执行模型调用
                .call()

                // 获取回答
                .content();
    }

    /**
     * 清理指定会话
     */
    public void clear(String conversationId) {
        chatMemory.clear(conversationId);
    }
}