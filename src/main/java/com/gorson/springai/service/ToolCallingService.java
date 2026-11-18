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
                        你是一个企业投资项目查询助手。

                        当用户询问项目列表时，
                        使用 searchProjects 工具查询。

                        根据用户提供的关键词、状态、
                        日期范围和分页要求组织工具参数。

                        不允许编造项目数据。
                        如果没有查询到项目，明确告知用户。

                        请使用中文回答。
                        """)

                .user(question)

                .tools(projectQueryToolCallback)

                .call()

                .content();
    }
}