package com.pbshop.springshop.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.pbshop.springshop.category.Category;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;

class PublicApiE2ETest extends AuthenticatedApiIntegrationSupport {

    @Test
    void healthCategoriesAndProductsArePublic() throws Exception {
        Category category = createCategory("Public", "public");
        Product product = createProduct(category, "PB Public Product", "pb-public");

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/categories/" + category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(category.getId()))
                .andExpect(jsonPath("$.data.name").value("Public"));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());

        mockMvc.perform(get("/api/v1/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(product.getId()));
    }
}
