package com.gorson.springai.security;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("local")
@Slf4j
public class LocalCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser requireCurrentUser() {

        return new CurrentUser(
                "user-001",
                "tenant-001",
                Set.of(
                        "PROJECT_READ",
                        "KNOWLEDGE_READ",
                        "KNOWLEDGE_WRITE"
                )
        );
    }
}