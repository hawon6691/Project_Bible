package com.pbshop.java.spring.maven.jpa.postgresql.e2e;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.java.spring.maven.jpa.postgresql.support.AuthenticatedApiIntegrationSupport;

class OpsDashboardThresholdsE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void opsDashboardReturnsAlertCountsAndMessages() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");

        mockMvc.perform(get("/api/v1/admin/ops-dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alertCount").value(1))
                .andExpect(jsonPath("$.data.alerts[0]", containsString("failed")));
    }
}
