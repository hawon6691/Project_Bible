package com.pbshop.java.spring.maven.jpa.postgresql.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.java.spring.maven.jpa.postgresql.support.AuthenticatedApiIntegrationSupport;

class AdminAuthorizationBoundaryE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void adminEndpointsRejectAnonymousAndNonAdminUsers() throws Exception {
        String userToken = createUserAndLogin("USER");

        mockMvc.perform(get("/api/v1/admin/ops-dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_401"));

        mockMvc.perform(get("/api/v1/admin/ops-dashboard/summary")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_403"));
    }
}
