package com.gorson.springai.service;

import com.gorson.springai.dto.request.ReportApplicationRequest;
import com.gorson.springai.dto.response.ReportApplicationResponse;
import com.gorson.springai.security.CurrentUser;
import com.gorson.springai.security.CurrentUserProvider;
import com.gorson.springai.security.ToolContextSupport;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Service;
import com.gorson.springai.dto.response.ReportApprovalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportApplicationService {

    private final CompiledGraph<AgentState> reportApprovalGraph;
    private final CurrentUserProvider currentUserProvider;
    private final ProjectQueryService projectQueryService;

    public ReportApplicationResponse apply(ReportApplicationRequest request)
            throws Exception {

        CurrentUser user = currentUserProvider.requireCurrentUser();
        ToolContextSupport.requireAuthority(user, "PROJECT_READ");

        String projectId = request.projectId().strip();
        if (!projectQueryService.existsInTenant(user.tenantId(), projectId)) {
            throw new SecurityException("项目不存在或无权访问");
        }

        String reportId = UUID.randomUUID().toString();
        var run = RunnableConfig.builder()
                .threadId(reportId)
                .build();

        var input = GraphInput.args(Map.of(
                "tenantId", user.tenantId(),
                "applicantId", user.userId(),
                "projectId", projectId,
                "question", request.question().strip()
        ));

        reportApprovalGraph.invoke(input, run);

        var snapshot = reportApprovalGraph.getState(run);
        if (!"generate".equals(snapshot.next())) {
            throw new IllegalStateException("报告流程未进入预期的审批等待点");
        }

        return new ReportApplicationResponse(reportId, "PENDING_APPROVAL");
    }

    public synchronized ReportApprovalResponse approve(String reportId)
            throws Exception {

        CurrentUser user = currentUserProvider.requireCurrentUser();
        if (!user.authorities().contains("REPORT_APPROVE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无报告审批权限");
        }

        var run = RunnableConfig.builder()
                .threadId(reportId)
                .build();

        var snapshot = reportApprovalGraph.stateOf(run)
                .filter(saved -> saved.state()
                        .<String>value("tenantId")
                        .filter(user.tenantId()::equals)
                        .isPresent())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "报告不存在或不可访问"));

        if (!"generate".equals(snapshot.next())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "报告已离开待审批状态");
        }

        var result = reportApprovalGraph.invoke(
                GraphInput.resume(Map.of(
                        "approved", true,
                        "approvedBy", user.userId()
                )),
                run
        ).orElseThrow(() -> new IllegalStateException("审批恢复后未取得流程状态"));

        String report = result.<String>value("report")
                .orElseThrow(() -> new IllegalStateException("流程未生成报告正文"));

        return new ReportApprovalResponse(reportId, "GENERATED", report);
    }
}