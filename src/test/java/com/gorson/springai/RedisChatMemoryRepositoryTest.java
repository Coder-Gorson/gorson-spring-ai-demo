package com.gorson.springai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisChatMemoryRepositoryTest {

    @Autowired
    private ChatMemoryRepository chatMemoryRepository;

    @Test
    void shouldUseRedisChatMemoryRepository() {
        assertThat(chatMemoryRepository)
                .isInstanceOf(RedisChatMemoryRepository.class);
    }
}