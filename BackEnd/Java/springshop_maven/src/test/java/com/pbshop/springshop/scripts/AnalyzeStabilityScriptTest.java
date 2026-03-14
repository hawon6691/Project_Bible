package com.pbshop.springshop.scripts;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;

class AnalyzeStabilityScriptTest extends AuthenticatedApiIntegrationSupport {

    @Test
    void repeatedHealthResponsesKeepStableEnvelope() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("UP"));
        }
    }
}
