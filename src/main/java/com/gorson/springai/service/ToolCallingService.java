package com.gorson.springai.service;

import com.gorson.springai.tool.ProjectQueryTools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ToolCallingService {

    private final ChatClient chatClient;

    private final ProjectQueryTools projectQueryTools;

    public ToolCallingService(
            ChatClient.Builder builder,
            ProjectQueryTools projectQueryTools) {

        this.chatClient = builder.build();
        this.projectQueryTools = projectQueryTools;
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

                .tools(projectQueryTools)

                .call()

                .content();
    }
}