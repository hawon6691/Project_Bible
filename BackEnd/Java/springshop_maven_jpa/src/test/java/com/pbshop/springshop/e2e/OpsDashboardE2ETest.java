package com.pbshop.springshop.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;

class OpsDashboardE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void opsDashboardSummaryIsAvailableToAdmins() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");

        mockMvc.perform(get("/api/v1/admin/ops-dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallStatus").value("degraded"))
                .andExpect(jsonPath("$.data.alerts").isArray());
    }
}
