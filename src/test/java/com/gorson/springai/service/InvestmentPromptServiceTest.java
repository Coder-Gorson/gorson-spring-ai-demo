package com.gorson.springai.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InvestmentPromptServiceTest {

    @Autowired
    private InvestmentPromptService investmentPromptService;

    @Test
    void shouldBuildSystemMessage() {

        Message message =
                investmentPromptService.buildSystemMessage(
                        "新能源投资项目",
                        "华星科技",
                        "新能源"
                );

        // 1. 验证消息角色
        assertEquals(
                MessageType.SYSTEM,
                message.getMessageType()
        );

        // 2. 获取消息文本
        String content = message.getText();

        assertNotNull(content);

        // 3. 验证动态变量替换
        assertTrue(content.contains("新能源投资项目"));
        assertTrue(content.contains("华星科技"));
        assertTrue(content.contains("新能源"));

        // 4. 验证模板变量已经替换
        assertFalse(content.contains("{projectName}"));
        assertFalse(content.contains("{companyName}"));
        assertFalse(content.contains("{industry}"));
    }
}