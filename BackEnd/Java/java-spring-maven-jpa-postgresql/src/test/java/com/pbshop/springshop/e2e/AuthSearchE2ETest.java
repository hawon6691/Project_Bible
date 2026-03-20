package com.pbshop.springshop.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.springshop.category.Category;
import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;

class AuthSearchE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void authenticatedProfileAndPublicSearchFlowWorkTogether() throws Exception {
        String userToken = createUserAndLogin("USER");
        Category category = createCategory("Search", "search");
        createProduct(category, "PB Search Product", "pb-search");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").exists());

        mockMvc.perform(get("/api/v1/products")
                        .param("search", "Search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("PB Search Product"));

        mockMvc.perform(get("/api/v1/rankings/keywords/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
