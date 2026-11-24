package com.gorson.springai.security;

import java.util.Set;

public record CurrentUser(
        String userId,
        String tenantId,
        Set<String> authorities
) {
}