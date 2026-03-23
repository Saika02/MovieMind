package com.lzz.backend.controller;

import com.lzz.backend.dto.AgentRecommendRequest;
import com.lzz.backend.dto.AgentRecommendResponse;
import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.service.AgentService;
import com.lzz.backend.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
@Tag(name = "智能体", description = "推荐与对话")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/recommend")
    @Operation(summary = "智能体推荐")
    public ApiResponse<AgentRecommendResponse> recommend(@RequestBody AgentRecommendRequest request, HttpServletRequest httpRequest) {
        SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(agentService.recommend(request));
    }
}
