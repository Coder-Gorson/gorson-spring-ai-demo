package com.gorson.springai.service;

import com.gorson.springai.dto.ProjectStatusStatistics;
import com.gorson.springai.dto.tool.ProjectSearchRequest;
import com.gorson.springai.dto.tool.ProjectSearchRequest.DateRange;
import com.gorson.springai.dto.tool.ProjectSearchRequest.PageRequest;
import com.gorson.springai.enums.ProjectStatus;
import com.gorson.springai.dto.tool.ProjectStatusStatisticsRequest;
import com.gorson.springai.dto.ProjectRecord;
import com.gorson.springai.dto.ProjectSearchResult;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
public class ProjectQueryService {

    private final Validator validator;
    private static final Logger log =
            LoggerFactory.getLogger(ProjectQueryService.class);

    public ProjectQueryService(Validator validator) {
        this.validator = validator;
    }

    private static final List<ProjectRecord> PROJECTS = List.of(

            new ProjectRecord(
                    "project-001",
                    "华星科技新能源项目",
                    "华星科技有限公司",
                    ProjectStatus.IN_PROGRESS,
                    LocalDate.of(2025, 3, 10),
                    "tenant-001"
            ),

            new ProjectRecord(
                    "project-002",
                    "华星科技光伏项目",
                    "华星科技有限公司",
                    ProjectStatus.PENDING,
                    LocalDate.of(2025, 8, 18),
                    "tenant-001"
            ),

            new ProjectRecord(
                    "project-003",
                    "海辰储能项目",
                    "海辰科技有限公司",
                    ProjectStatus.COMPLETED,
                    LocalDate.of(2025, 11, 20),
                    "tenant-002"
            ),

            new ProjectRecord(
                    "project-004",
                    "华星科技储能项目",
                    "华星科技有限公司",
                    ProjectStatus.COMPLETED,
                    LocalDate.of(2024, 12, 20),
                    "tenant-001"
            )
    );

    public ProjectSearchResult searchProjects(
            ProjectSearchRequest request) {
        log.info("执行项目查询工具，参数：{}", request);

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
     * 按租户查询项目
     */
    public ProjectSearchResult searchProjects(
            ProjectSearchRequest request,
            String tenantId) {

        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("租户ID不能为空");
        }

        return searchProjectsInternal(request, tenantId);
    }

    private ProjectSearchResult searchProjectsInternal(
            ProjectSearchRequest request,
            String tenantId) {
        log.info("执行项目查询工具，参数：{}", request);

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
                        tenantId == null
                                || tenantId.equals(project.tenantId())
                )

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

    public ProjectStatusStatistics countProjectsByStatus(
            ProjectStatusStatisticsRequest request) {

        return countProjectsByStatusInternal(request, null);
    }

    public ProjectStatusStatistics countProjectsByStatus(
            ProjectStatusStatisticsRequest request,
            String tenantId) {

        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("租户ID不能为空");
        }

        return countProjectsByStatusInternal(request, tenantId);
    }

    private ProjectStatusStatistics countProjectsByStatusInternal(
            ProjectStatusStatisticsRequest request,
            String tenantId) {

        if (request == null) {
            throw new IllegalArgumentException("统计参数不能为空");
        }
        log.info("执行项目状态统计工具，项目ID：{}", request.projectIds());

        Set<ConstraintViolation<ProjectStatusStatisticsRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                    violations.stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .reduce((a, b) -> a + "; " + b)
                            .orElse("参数校验失败")
            );
        }

        Set<String> projectIds = new HashSet<>(request.projectIds());

        List<ProjectRecord> matched = PROJECTS.stream()

                .filter(project ->
                        tenantId == null
                                || tenantId.equals(project.tenantId())
                )

                .filter(project -> projectIds.contains(project.projectId()))

                .toList();

        Map<String, Long> counts = new LinkedHashMap<>();

        for (ProjectStatus status : ProjectStatus.values()) {
            counts.put(status.name(), 0L);
        }

        for (ProjectRecord project : matched) {
            counts.merge(
                    project.status().name(),
                    1L,
                    Long::sum
            );
        }

        return new ProjectStatusStatistics(
                matched.size(),
                counts
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

}