package com.gorson.springai.dto.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.gorson.springai.enums.ProjectStatus;

import java.util.List;

@JsonClassDescription("企业投资项目查询参数")
public record ProjectSearchRequest(

        @JsonPropertyDescription(
                "项目名称、项目编号或企业名称关键词，最多100个字符"
        )
        @JsonProperty(required = false)
        @Size(max = 100, message = "关键词不能超过100个字符")
        String keyword,

        @JsonPropertyDescription(
                "项目状态列表，可选值：PENDING、IN_PROGRESS、COMPLETED、CANCELLED"
        )
        @JsonProperty(required = false)
        @Size(max = 4, message = "项目状态最多选择4种")
        List<ProjectStatus> statuses,

        @JsonPropertyDescription(
                "项目创建日期范围，可选参数"
        )
        @JsonProperty(required = false)
        @Valid
        DateRange createDateRange,

        @JsonPropertyDescription(
                "分页参数，可选参数，默认第一页，每页20条"
        )
        @JsonProperty(required = false)
        @Valid
        PageRequest page
) {

    @JsonClassDescription("项目创建日期范围")
    public record DateRange(

            @JsonPropertyDescription("开始日期，格式yyyy-MM-dd")
            @NotBlank(message = "开始日期不能为空")
            @Pattern(
                    regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                    message = "开始日期格式必须为yyyy-MM-dd"
            )
            String startDate,

            @JsonPropertyDescription("结束日期，格式yyyy-MM-dd")
            @NotBlank(message = "结束日期不能为空")
            @Pattern(
                    regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                    message = "结束日期格式必须为yyyy-MM-dd"
            )
            String endDate
    ) {
    }

    @JsonClassDescription("分页参数")
    public record PageRequest(

            @JsonPropertyDescription("页码，从1开始，最大10000")
            @NotNull(message = "页码不能为空")
            @Min(value = 1, message = "页码不能小于1")
            @Max(value = 10000, message = "页码不能大于10000")
            Integer pageNumber,

            @JsonPropertyDescription("每页数量，范围1到100")
            @NotNull(message = "每页数量不能为空")
            @Min(value = 1, message = "每页数量不能小于1")
            @Max(value = 100, message = "每页数量不能大于100")
            Integer pageSize
    ) {
    }
}