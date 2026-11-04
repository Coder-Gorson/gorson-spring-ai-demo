package com.gorson.springai.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InvestmentPromptService {

    @Value("classpath:/prompts/investment-system.st")
    private Resource systemPromptResource;

    public Message buildSystemMessage(
            String projectName,
            String companyName,
            String industry) {

        SystemPromptTemplate template =
                new SystemPromptTemplate(systemPromptResource);

        return template.createMessage(
                Map.of(
                        "projectName", projectName,
                        "companyName", companyName,
                        "industry", industry
                )
        );
    }
}