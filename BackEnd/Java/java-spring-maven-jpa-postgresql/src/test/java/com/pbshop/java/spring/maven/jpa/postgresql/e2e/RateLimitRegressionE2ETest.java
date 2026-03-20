package com.pbshop.java.spring.maven.jpa.postgresql.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.java.spring.maven.jpa.postgresql.support.AuthenticatedApiIntegrationSupport;

class RateLimitRegressionE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void repeatedPublicHealthRequestsRemainStable() throws Exception {
        for (int i = 0; i < 25; i++) {
            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(status().isOk());
        }
    }
}
