package com.gorson.springai.config;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Configuration(proxyBeanMethods = false)
public class ReportApprovalConfiguration {

    @Bean
    public CompiledGraph<AgentState> reportApprovalGraph() throws Exception {
        var workflow = new StateGraph<>(AgentState::new);

        workflow.addNode("prepare", node_async(state -> {
            String projectId = state.<String>value("projectId").orElseThrow();
            return Map.of("material", projectId + " 的报告资料");
        }));

        workflow.addNode("generate", node_async(state -> {
            if (!Boolean.TRUE.equals(state.value("approved").orElse(false))) {
                throw new IllegalStateException("报告尚未批准");
            }

            String material = state.<String>value("material").orElseThrow();
            return Map.of("report", "根据以下资料生成报告：" + material);
        }));

        workflow.addEdge(START, "prepare");
        workflow.addEdge("prepare", "generate");
        workflow.addEdge("generate", END);

        return workflow.compile(CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .interruptBefore("generate")
                .build());
    }
}