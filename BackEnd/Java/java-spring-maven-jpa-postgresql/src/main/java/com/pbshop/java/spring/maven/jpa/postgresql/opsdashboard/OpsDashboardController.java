package com.pbshop.java.spring.maven.jpa.postgresql.opsdashboard;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/admin/ops-dashboard")
public class OpsDashboardController {

    private final OpsDashboardService opsDashboardService;

    public OpsDashboardController(OpsDashboardService opsDashboardService) {
        this.opsDashboardService = opsDashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(opsDashboardService.summary(principal));
    }
}
