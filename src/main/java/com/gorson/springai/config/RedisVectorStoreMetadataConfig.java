package com.gorson.springai.config;

import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RedisVectorStoreMetadataConfig {

    @Bean
    public List<RedisVectorStore.MetadataField> redisMetadataFields() {

        return List.of(
                RedisVectorStore.MetadataField.tag("tenantId"),
                RedisVectorStore.MetadataField.tag("projectId"),
                RedisVectorStore.MetadataField.tag("source"),
                RedisVectorStore.MetadataField.numeric("page"),
                RedisVectorStore.MetadataField.tag("chunkId")
        );
    }
}