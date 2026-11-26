package com.gorson.springai.service;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
@RequiredArgsConstructor
public class McpChatService {

    private final ChatModel chatModel;

    public String chat(String question) {
        var transport = HttpClientStreamableHttpTransport
                .builder("http://127.0.0.1:18880")
                .endpoint("/mcp")
                .build();

        try (McpSyncClient mcpClient = McpClient.sync(transport).build()) {
            mcpClient.initialize();

            var mcpTools = SyncMcpToolCallbackProvider.builder()
                    .mcpClients(mcpClient)
                    .build();

            return ChatClient.create(chatModel)
                    .prompt()
                    .system("""
                            你是项目查询助手。
                            回答项目数据问题时，请先调用可用的项目查询工具。
                            只根据工具返回的记录回答，并使用中文。
                            """)
                    .user(question)
                    .tools(mcpTools)
                    .call()
                    .content();
        }
    }
}