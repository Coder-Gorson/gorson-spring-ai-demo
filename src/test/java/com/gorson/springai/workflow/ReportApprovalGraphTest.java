package com.gorson.springai.workflow;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportApprovalGraphTest {

    @Test
    void shouldResumeApprovedReportWithoutPreparingTwice() throws Exception {
        var prepareCalls = new AtomicInteger();
        var generateCalls = new AtomicInteger();

        var workflow = new StateGraph<>(AgentState::new);

        workflow.addNode("prepare", node_async(state -> {
            prepareCalls.incrementAndGet();
            return Map.of("material", "project-001 的报告资料");
        }));

        workflow.addNode("generate", node_async(state -> {
            if (!Boolean.TRUE.equals(state.value("approved").orElse(false))) {
                throw new IllegalStateException("报告尚未批准");
            }

            String material = state.<String>value("material").orElseThrow();
            generateCalls.incrementAndGet();
            return Map.of("report", "根据以下资料生成报告：" + material);
        }));

        workflow.addEdge(START, "prepare");
        workflow.addEdge("prepare", "generate");
        workflow.addEdge("generate", END);

        var graph = workflow.compile(CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .interruptBefore("generate")
                .build());

        var run = RunnableConfig.builder()
                .threadId("report-approval-001")
                .build();

        // 第一次执行：准备资料，在生成报告之前暂停。
        graph.invoke(GraphInput.noArgs(), run);

        assertEquals(1, prepareCalls.get());
        assertEquals(0, generateCalls.get());

        // 模拟审批通过。第二次执行沿用同一个图实例和工作流标识。
        var result = graph.invoke(
                GraphInput.resume(Map.of("approved", true)),
                run
        ).orElseThrow();

        assertEquals(1, prepareCalls.get());
        assertEquals(1, generateCalls.get());
        assertEquals(
                "根据以下资料生成报告：project-001 的报告资料",
                result.<String>value("report").orElseThrow()
        );
    }
}