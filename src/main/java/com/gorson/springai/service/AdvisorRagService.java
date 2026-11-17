package com.gorson.springai.service;

import com.gorson.springai.dto.RagAskResponse;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdvisorRagService {

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    public AdvisorRagService(
            VectorStore vectorStore,
            ChatClient.Builder chatClientBuilder) {

        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 使用 QuestionAnswerAdvisor 实现 RAG
     */
    public RagAskResponse ask(
            String question,
            String projectId,
            int topK,
            double threshold) {

        // 1. 构建项目 Metadata 过滤条件
        var filterExpression = new FilterExpressionBuilder()
                .eq("projectId", projectId)
                .build();

        // 2. 构建检索参数
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(topK)
                .similarityThreshold(threshold)
                .filterExpression(filterExpression)
                .build();

        // 3. 创建 QuestionAnswerAdvisor
        QuestionAnswerAdvisor advisor =
                QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build();

        // 4. 调用 ChatClient
        ChatClientResponse response = chatClient.prompt()

                .system("""
                        你是一个专业的企业知识库问答助手。

                        请遵守以下规则：
                        1. 根据提供的参考资料回答问题。
                        2. 不允许编造参考资料中不存在的数据。
                        3. 如果资料不足以回答，明确说明。
                        4. 回答应准确、简洁。
                        """)

                .advisors(advisor)

                .user(question)

                .call()
                .chatClientResponse();

        // 5. 获取模型回答
        String answer = response.chatResponse() == null
                || response.chatResponse().getResult() == null
                ? ""
                : response.chatResponse()
                .getResult()
                .getOutput()
                .getText();

        // 6. 获取 Advisor 检索到的 Document
        List<Document> documents = getRetrievedDocuments(response);

        // 7. 构建引用来源
        List<RagAskResponse.SourceReference> sources =
                buildSources(documents);

        return new RagAskResponse(
                question,
                answer,
                documents.size(),
                sources
        );
    }

    /**
     * 从 Advisor 执行上下文中获取检索结果
     */
    private List<Document> getRetrievedDocuments(
            ChatClientResponse response) {

        Object value = response.context().get(
                QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS
        );

        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .toList();
    }

    /**
     * 构建引用来源
     */
    private List<RagAskResponse.SourceReference> buildSources(
            List<Document> documents) {

        return java.util.stream.IntStream.range(0, documents.size())
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