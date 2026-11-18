package com.gorson.springai.service;

import com.gorson.springai.dto.tool.ProjectSearchRequest;
import com.gorson.springai.dto.tool.ProjectSearchRequest.DateRange;
import com.gorson.springai.dto.tool.ProjectSearchRequest.PageRequest;
import com.gorson.springai.enums.ProjectStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class ProjectQueryService {

    private final Validator validator;

    public ProjectQueryService(Validator validator) {
        this.validator = validator;
    }

    private static final List<ProjectRecord> PROJECTS = List.of(

            new ProjectRecord(
                    "project-001",
                    "华星科技新能源项目",
                    "华星科技有限公司",
                    ProjectStatus.IN_PROGRESS,
                    LocalDate.of(2025, 3, 10)
            ),

            new ProjectRecord(
                    "project-002",
                    "华星科技光伏项目",
                    "华星科技有限公司",
                    ProjectStatus.PENDING,
                    LocalDate.of(2025, 8, 18)
            ),

            new ProjectRecord(
                    "project-003",
                    "海辰储能项目",
                    "海辰科技有限公司",
                    ProjectStatus.COMPLETED,
                    LocalDate.of(2025, 11, 20)
            ),

            new ProjectRecord(
                    "project-004",
                    "华星科技储能项目",
                    "华星科技有限公司",
                    ProjectStatus.COMPLETED,
                    LocalDate.of(2024, 12, 20)
            )
    );

    public ProjectSearchResult searchProjects(
            ProjectSearchRequest request) {

        // 1. 参数校验
        validateRequest(request);

        // 2. 提取查询条件
        String keyword = request.keyword();

        List<ProjectStatus> statuses = request.statuses();

        DateRange dateRange = request.createDateRange();

        // 3. 至少提供一个有效查询条件
        boolean hasCondition =
                StringUtils.hasText(keyword)
                        || (statuses != null && !statuses.isEmpty())
                        || dateRange != null;

        if (!hasCondition) {
            throw new IllegalArgumentException(
                    "请至少提供关键词、项目状态或创建日期范围中的一个查询条件"
            );
        }

        // 4. 日期范围处理
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (dateRange != null) {
            try {
                startDate = LocalDate.parse(dateRange.startDate());
                endDate = LocalDate.parse(dateRange.endDate());
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException(
                        "日期不是有效的日历日期，请检查日期范围"
                );
            }

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException(
                        "开始日期不能晚于结束日期"
                );
            }
        }

        // 5. 执行条件过滤
        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        List<ProjectRecord> matched = PROJECTS.stream()

                .filter(project ->
                        !StringUtils.hasText(keyword)
                                || project.projectId().contains(keyword)
                                || project.projectName().contains(keyword)
                                || project.companyName().contains(keyword)
                )

                .filter(project ->
                        statuses == null
                                || statuses.isEmpty()
                                || statuses.contains(project.status())
                )

                .filter(project ->
                        finalStartDate == null
                                || (!project.createdAt().isBefore(finalStartDate)
                                && !project.createdAt().isAfter(finalEndDate))
                )

                .sorted(
                        Comparator.comparing(ProjectRecord::createdAt)
                                .reversed()
                )

                .toList();

        // 6. 分页处理
        PageRequest page = request.page();

        int pageNumber = page == null ? 1 : page.pageNumber();
        int pageSize = page == null ? 20 : page.pageSize();

        int fromIndex = Math.min(
                (pageNumber - 1) * pageSize,
                matched.size()
        );

        int toIndex = Math.min(
                fromIndex + pageSize,
                matched.size()
        );

        List<ProjectRecord> records =
                matched.subList(fromIndex, toIndex);

        // 7. 返回查询结果
        return new ProjectSearchResult(
                matched.size(),
                pageNumber,
                pageSize,
                records
        );
    }

    /**
     * Bean Validation 参数校验
     */
    private void validateRequest(ProjectSearchRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }

        Set<ConstraintViolation<ProjectSearchRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {

            String message = violations.stream()

                    .map(v ->
                            v.getPropertyPath()
                                    + ": "
                                    + v.getMessage()
                    )

                    .reduce((a, b) -> a + "; " + b)
                    .orElse("参数校验失败");

            throw new IllegalArgumentException(
                    "工具参数校验失败：" + message
            );
        }
    }

    public record ProjectRecord(
            String projectId,
            String projectName,
            String companyName,
            ProjectStatus status,
            LocalDate createdAt
    ) {
    }

    public record ProjectSearchResult(
            long total,
            int pageNumber,
            int pageSize,
            List<ProjectRecord> records
    ) {
    }
}