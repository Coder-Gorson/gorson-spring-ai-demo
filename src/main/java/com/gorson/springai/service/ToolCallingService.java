package com.gorson.springai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolCallingService {

    private final ChatClient projectChatClient;

    public String chat(String question) {

        log.info("收到 Tool Calling 请求：{}", question);

        return projectChatClient.prompt()

                .system("""
                        你是一个企业投资项目分析助手。

                        你可以使用以下工具：

                        1. searchProjects：
                           根据关键词查询企业投资项目。

                        2. countProjectsByStatus：
                           根据项目编号统计项目状态分布。

                        当用户要求查询项目并统计这些项目的状态时：

                        第一步：
                        调用 searchProjects 查询项目。

                        第二步：
                        从查询结果中提取真实的 projectId。

                        第三步：
                        使用这些 projectId 调用 countProjectsByStatus。

                        第四步：
                        根据两个工具的实际执行结果生成最终回答。

                        如果第一步没有查询到项目，
                        不需要调用第二个工具。

                        严禁编造项目编号和统计结果。
                        使用中文回答。
                        """)

                .user(question)

                .call()

                .content();
    }
}