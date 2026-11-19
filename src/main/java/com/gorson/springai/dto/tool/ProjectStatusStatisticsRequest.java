package com.gorson.springai.dto.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonClassDescription("项目状态统计参数")
public record ProjectStatusStatisticsRequest(

        @JsonPropertyDescription(
                "需要统计的项目编号列表，必须来自项目查询工具的返回结果"
        )
        @NotEmpty(message = "项目编号列表不能为空")
        @Size(max = 100, message = "最多统计100个项目")
        List<@NotBlank String> projectIds

) {
}