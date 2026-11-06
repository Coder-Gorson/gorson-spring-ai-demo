package com.gorson.springai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class GorsonSpringAiLabApplicationTests {

	@Autowired
	private ChatMemory chatMemory;

	@Test
	void contextLoads() {
	}



	@Test
	void shouldLoadChatMemory() {
		assertNotNull(chatMemory);
	}
}
