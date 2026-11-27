package com.gorson.springai.controller;

import com.gorson.springai.dto.request.ProjectWorkflowRequest;
import com.gorson.springai.dto.response.ProjectWorkflowResponse;
import com.gorson.springai.service.ProjectWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflow/project")
@RequiredArgsConstructor
public class ProjectWorkflowController {

    private final ProjectWorkflowService projectWorkflowService;

    @PostMapping("/ask")
    public ProjectWorkflowResponse ask(
            @Valid @RequestBody ProjectWorkflowRequest request) {
        return projectWorkflowService.ask(request);
    }
}