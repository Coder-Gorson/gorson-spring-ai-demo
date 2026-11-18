package com.gorson.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

@Service
public class ToolCallingService {

    private final ChatClient chatClient;

    private final ToolCallback projectQueryToolCallback;

    public ToolCallingService(
            ChatClient.Builder builder,
            ToolCallback projectQueryToolCallback) {

        this.chatClient = builder.build();
        this.projectQueryToolCallback = projectQueryToolCallback;
    }

    public String chat(String question) {

        return chatClient.prompt()

                .system("""
                        你是一个企业投资项目助手。

                        当用户询问项目基本信息时，
                        使用提供的项目查询工具获取真实数据。

                        不允许编造项目数据。
                        如果项目不存在，明确告知用户。
                        请使用中文回答。
                        """)

                .user(question)

                .tools(projectQueryToolCallback)

                .call()

                .content();
    }
}