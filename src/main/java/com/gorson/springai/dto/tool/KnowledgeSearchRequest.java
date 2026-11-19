package com.gorson.springai.dto.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import jakarta.validation.constraints.NotBlank;

@JsonClassDescription("企业知识库搜索请求")
public record KnowledgeSearchRequest(

        @JsonPropertyDescription(
                "需要在企业知识库中检索的自然语言问题，描述需要查找的企业资料或信息"
        )
        @NotBlank(message = "检索问题不能为空")
        String query

) {
}