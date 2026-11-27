package com.gorson.springai.controller;

import com.gorson.springai.dto.request.ReportApplicationRequest;
import com.gorson.springai.dto.response.ReportApplicationResponse;
import com.gorson.springai.dto.response.ReportApprovalResponse;
import com.gorson.springai.service.ReportApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportApplicationService reportApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportApplicationResponse apply(
            @Valid @RequestBody ReportApplicationRequest request)
            throws Exception {

        return reportApplicationService.apply(request);
    }

    @PostMapping("/{reportId}/approve")
    public ReportApprovalResponse approve(
            @PathVariable("reportId") String reportId) throws Exception {

        return reportApplicationService.approve(reportId);
    }
}