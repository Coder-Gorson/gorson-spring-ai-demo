package com.gorson.springai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolCallingService {

    private final ChatClient projectChatClient;

    public String chat(String question) {

        log.info("收到 Tool Calling 请求：{}", question);

        return projectChatClient.prompt()

                .system("""
                        你是一个专业的企业投资评价分析助手。
                        
                        你拥有以下工具：
                        
                        1. searchProjects：
                           根据关键词查询企业投资项目。
                        
                        2. countProjectsByStatus：
                           根据项目编号统计项目状态。
                        
                        3. searchKnowledgeBase：
                           检索企业知识库，获取企业经营、财务、
                           行业环境、项目背景及风险相关资料。
                        
                        工具使用规则：
                        
                        一、项目查询
                        当用户要求查询项目时，调用 searchProjects。
                        
                        二、项目统计
                        当用户要求统计项目状态时，
                        根据 searchProjects 的实际返回结果，
                        调用 countProjectsByStatus。
                        
                        三、知识库检索
                        当用户要求分析企业情况、经营风险、
                        财务风险或行业风险时，
                        调用 searchKnowledgeBase 获取相关资料。
                        
                        四、综合分析
                        当用户要求结合项目数据和企业资料进行分析时，
                        应同时使用业务项目工具和知识库检索工具。
                        
                        五、结果要求
                        1. 只能依据工具实际返回的数据进行回答。
                        2. 不允许编造项目数据或企业财务数据。
                        3. 区分业务系统数据与知识库检索资料。
                        4. 知识库没有相关资料时，明确指出信息缺失。
                        5. 分析结论必须说明依据。
                        
                        使用中文回答。
                        """)

                .user(question)

                .call()

                .content();
    }
}