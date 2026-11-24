package com.gorson.springai.security;

import lombok.experimental.UtilityClass;

import org.springframework.ai.chat.model.ToolContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ToolContextSupport {

    public CurrentUser requireCurrentUser(ToolContext toolContext) {

        if (toolContext == null) {
            throw new SecurityException("缺少工具执行上下文");
        }

        Map<String, Object> context = toolContext.getContext();

        String userId = requireString(context, "userId");
        String tenantId = requireString(context, "tenantId");

        Object authoritiesValue = context.get("authorities");

        if (!(authoritiesValue instanceof Collection<?> authorities)) {
            throw new SecurityException("缺少用户权限信息");
        }

        Set<String> authoritySet = authorities.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toUnmodifiableSet());

        return new CurrentUser(
                userId,
                tenantId,
                authoritySet
        );
    }

    public void requireAuthority(
            CurrentUser user,
            String authority) {

        if (!user.authorities().contains(authority)) {
            throw new SecurityException("无权执行该操作");
        }
    }

    private String requireString(
            Map<String, Object> context,
            String key) {

        Object value = context.get(key);

        if (!(value instanceof String text) || text.isBlank()) {
            throw new SecurityException(
                    "缺少必要的用户上下文：" + key
            );
        }

        return text;
    }
}