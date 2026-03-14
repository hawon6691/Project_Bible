package com.pbshop.springshop.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;

class SecurityRegressionE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void publicDocsStayOpenAndProtectedAdminEndpointsStayGuarded() throws Exception {
        String userToken = createUserAndLogin("USER");

        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths").exists());

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_401"));

        mockMvc.perform(get("/api/v1/admin/queues/stats")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_403"));
    }
}
