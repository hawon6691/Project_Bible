package com.pbshop.springshop.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.pbshop.springshop.category.Category;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.searchsync.SearchIndexOutbox;
import com.pbshop.springshop.searchsync.SearchIndexOutboxRepository;
import com.pbshop.springshop.support.AuthenticatedApiIntegrationSupport;
import org.springframework.beans.factory.annotation.Autowired;

class AdminPlatformE2ETest extends AuthenticatedApiIntegrationSupport {

    @Autowired
    private SearchIndexOutboxRepository searchIndexOutboxRepository;

    @Test
    void adminPlatformEndpointsWorkAcrossOpsFlow() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        Category category = createCategory("Ops", "ops");
        Product product = createProduct(category, "PB Ops Product", "pb-ops");
        createProductSpec(product, "memory", "32GB");
        Seller seller = createSeller("PB Seller", "pb-seller");
        createPriceEntry(product, seller, new BigDecimal("129000"));

        SearchIndexOutbox outbox = new SearchIndexOutbox();
        outbox.setEntityType("PRODUCT");
        outbox.setEntityId(product.getId());
        outbox.setStatus("FAILED");
        searchIndexOutboxRepository.save(outbox);

        mockMvc.perform(get("/api/v1/errors/codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3));

        mockMvc.perform(post("/api/v1/admin/settings/extensions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "extensions": ["jpg", "png"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.extensions[0]").value("jpg"));

        mockMvc.perform(post("/api/v1/admin/query/products/" + product.getId() + "/sync")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(product.getId()));

        mockMvc.perform(post("/api/v1/search/admin/index/outbox/requeue-failed?limit=5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requeuedCount").value(1));
    }
}
