package com.gorson.springai.service;

import com.gorson.springai.security.CurrentUser;
import com.gorson.springai.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolCallingService {

    private final ChatClient projectChatClient;
    private final CurrentUserProvider currentUserProvider;

    public String chat(String question) {

        // 1. 从服务端获取当前用户
        CurrentUser currentUser =
                currentUserProvider.requireCurrentUser();

        // 2. 构造可信上下文
        Map<String, Object> toolContext = Map.of(
                "userId", currentUser.userId(),
                "tenantId", currentUser.tenantId(),
                "authorities", currentUser.authorities()
        );

        log.info(
                "Tool Calling 请求，userId={}, tenantId={}",
                currentUser.userId(),
                currentUser.tenantId()
        );

        // 3. 发起模型请求
        return projectChatClient.prompt()

                .toolContext(toolContext)

                .system("""
                    你是一个企业投资项目分析助手。

                    你可以使用项目查询、项目统计和知识库检索工具。

                    只根据工具实际返回的数据回答问题。
                    
                    使用中文回答。
                    """)

                .user(question)

                .call()

                .content();
    }
}