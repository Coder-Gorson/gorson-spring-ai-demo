package com.gorson.springai.dto;

import com.gorson.springai.enums.ProjectStatus;

import java.time.LocalDate;

public record ProjectRecord(
        String projectId,
        String projectName,
        String companyName,
        ProjectStatus status,
        LocalDate createdAt,
        String tenantId
) {}