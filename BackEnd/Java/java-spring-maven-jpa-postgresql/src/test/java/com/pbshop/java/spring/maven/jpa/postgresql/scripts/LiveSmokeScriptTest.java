package com.pbshop.java.spring.maven.jpa.postgresql.scripts;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.java.spring.maven.jpa.postgresql.category.Category;
import com.pbshop.java.spring.maven.jpa.postgresql.support.AuthenticatedApiIntegrationSupport;

class LiveSmokeScriptTest extends AuthenticatedApiIntegrationSupport {

    @Test
    void publicEndpointsRespondForSmokeCheck() throws Exception {
        Category category = createCategory("Smoke", "smoke");
        createProduct(category, "PB Smoke Product", "pb-smoke");

        mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/categories/" + category.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk());
        mockMvc.perform(get("/docs/openapi")).andExpect(status().isOk());
    }
}
