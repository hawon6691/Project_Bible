package com.pbshop.springshop.e2e;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;

class ContractPublicApiE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void publicApiUsesStandardEnvelopeAndRequestId() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .header("X-Request-Id", "contract-public-api-e2e"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "contract-public-api-e2e"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.meta.timestamp", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.meta.locale").value("ko-KR"))
                .andExpect(jsonPath("$.meta.currency").value("KRW"))
                .andExpect(jsonPath("$.meta.requestId").value("contract-public-api-e2e"));
    }
}
