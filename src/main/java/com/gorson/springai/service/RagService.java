package com.gorson.springai.service;

import com.gorson.springai.dto.RagAskResponse;

import com.gorson.springai.security.CurrentUser;
import com.gorson.springai.security.CurrentUserProvider;
import com.gorson.springai.security.ToolContextSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStoreService vectorStoreService;

    private final ChatClient chatClient;

    private final CurrentUserProvider currentUserProvider;

    /**
     * RAG 问答
     */
    public RagAskResponse ask(
            String question,
            String projectId,
            int topK,
            double threshold) {

        // 1. 获取当前登录用户
        CurrentUser currentUser =
                currentUserProvider.requireCurrentUser();

        // 2. 校验知识库读取权限
        ToolContextSupport.requireAuthority(
                currentUser,
                "KNOWLEDGE_READ"
        );

        // 3. 按租户 + 项目进行向量检索
        List<Document> documents =
                vectorStoreService.searchByTenantAndProject(
                        question,
                        topK,
                        currentUser.tenantId(),
                        projectId,
                        threshold
                );

        // 4. 调用 DeepSeek 生成答案
        String answer = chatClient.prompt()

                .system("""
                        你是一个专业的企业知识库问答助手。

                        请严格遵守以下规则：

                        1. 只能根据提供的参考资料回答问题。
                        2. 不允许编造资料中不存在的数据。
                        3. 如果参考资料不足以回答问题，明确说明资料不足。
                        4. 回答中的关键事实必须标注引用编号，
                           例如：[资料1]。
                        5. 不允许伪造引用编号。
                        6. 回答应当准确、简洁、清晰。
                        """)

                .user(user -> user
                        .text("""
                                用户问题：

                                {question}

                                以下是从知识库检索到的参考资料：

                                {context}

                                请根据参考资料回答用户问题，
                                并在关键结论后标注资料编号。
                                """)
                        .param("question", question)
                        .param("context", documents))

                .call()
                .content();

        // 5. 组装引用来源
        List<RagAskResponse.SourceReference> sources =
                buildSources(documents);

        // 6. 返回结果
        return new RagAskResponse(
                question,
                answer,
                documents.size(),
                sources
        );
    }

    /**
     * 组装参考资料上下文
     */
    private String buildContext(List<Document> documents) {

        return IntStream.range(0, documents.size())
                .mapToObj(index -> {

                    Document document = documents.get(index);

                    Map<String, Object> metadata =
                            document.getMetadata();

                    String source = String.valueOf(
                            metadata.getOrDefault("source", "未知")
                    );

                    Object page = metadata.getOrDefault(
                            "page", "未知"
                    );

                    return """
                            [资料%d]
                            文档ID：%s
                            来源：%s
                            页码：%s
                            内容：%s
                            """.formatted(
                            index + 1,
                            document.getId(),
                            source,
                            page,
                            document.getText()
                    );
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * 构建引用来源
     */
    private List<RagAskResponse.SourceReference> buildSources(
            List<Document> documents) {

        return IntStream.range(0, documents.size())
                .mapToObj(index -> {

                    Document document = documents.get(index);

                    Map<String, Object> metadata =
                            document.getMetadata();

                    String source = String.valueOf(
                            metadata.getOrDefault("source", "未知")
                    );

                    Object pageValue = metadata.get("page");

                    Integer page = pageValue instanceof Number number
                            ? number.intValue()
                            : null;

                    return new RagAskResponse.SourceReference(
                            "资料" + (index + 1),
                            document.getId(),
                            source,
                            page
                    );
                })
                .toList();
    }
}