package com.pbshop.springshop.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.support.ApiIntegrationSupport;

@Import(ApiContractSupportTest.TestController.class)
@ActiveProfiles("test")
class ApiContractSupportTest extends ApiIntegrationSupport {

    @Test
    void unauthorizedRequestsUseApiEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/test/common/secure"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_401"));
    }

    @Test
    @WithMockUser(username = "tester")
    void businessExceptionUsesApiEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/test/common/business").header("X-Request-Id", "req-123"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", "req-123"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.meta.requestId").value("req-123"))
                .andExpect(jsonPath("$.error.code").value("COMMON_400"))
                .andExpect(jsonPath("$.error.details.reason").value("forced"));
    }

    @Test
    @WithMockUser(username = "tester")
    void validationExceptionUsesApiEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/test/common/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_400_VALIDATION"))
                .andExpect(jsonPath("$.error.details.name").exists());
    }

    @RestController
    @RequestMapping("/api/v1/test/common")
    static class TestController {

        @GetMapping("/secure")
        String secure() {
            return "ok";
        }

        @GetMapping("/business")
        String business() {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "강제 예외", java.util.Map.of("reason", "forced"));
        }

        @PostMapping("/validation")
        String validation(@Valid @RequestBody ValidationRequest request) {
            return request.name();
        }
    }

    record ValidationRequest(@NotBlank(message = "name is required") String name) {
    }
}

