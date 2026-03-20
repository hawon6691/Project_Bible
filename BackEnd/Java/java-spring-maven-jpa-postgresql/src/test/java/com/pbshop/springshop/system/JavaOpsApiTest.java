package com.pbshop.springshop.system;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbshop.springshop.category.Category;
import com.pbshop.springshop.category.CategoryRepository;
import com.pbshop.springshop.product.PriceEntry;
import com.pbshop.springshop.product.PriceEntryRepository;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.product.ProductSpec;
import com.pbshop.springshop.product.ProductSpecRepository;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.product.SellerRepository;
import com.pbshop.springshop.searchsync.SearchIndexOutbox;
import com.pbshop.springshop.searchsync.SearchIndexOutboxRepository;
import com.pbshop.springshop.support.ApiIntegrationSupport;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@Transactional
class JavaOpsApiTest extends ApiIntegrationSupport {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSpecRepository productSpecRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private PriceEntryRepository priceEntryRepository;
    @Autowired private SearchIndexOutboxRepository searchIndexOutboxRepository;

    @Test
    void adminSettingsHealthErrorCodesAndResilienceWork() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("java"));

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

        mockMvc.perform(get("/api/v1/resilience/circuit-breakers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("search-sync"));

        mockMvc.perform(post("/api/v1/resilience/circuit-breakers/crawler/reset")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("crawler"));
    }

    @Test
    void queueQuerySearchSyncAndCrawlerFlowWorks() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        Category category = createCategory("Ops", "ops");
        Product product = createProduct(category, "PB Ops Product", "pb-ops");
        createProductSpec(product, "memory", "32GB");
        createPriceEntry(product, createSeller("PB Seller", "pb-seller"), new BigDecimal("129000"));

        SearchIndexOutbox outbox = new SearchIndexOutbox();
        outbox.setEntityType("PRODUCT");
        outbox.setEntityId(product.getId());
        outbox.setStatus("FAILED");
        searchIndexOutboxRepository.save(outbox);

        mockMvc.perform(get("/api/v1/admin/queues/supported")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0]").value("default"));

        mockMvc.perform(post("/api/v1/admin/query/products/" + product.getId() + "/sync")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(product.getId()));

        mockMvc.perform(get("/api/v1/query/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(product.getId()));

        mockMvc.perform(get("/api/v1/search/admin/index/outbox/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.failed").value(1));

        mockMvc.perform(post("/api/v1/search/admin/index/outbox/requeue-failed?limit=5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requeuedCount").value(1));

        MvcResult createdJob = mockMvc.perform(post("/api/v1/crawler/admin/jobs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "PB 크롤러",
                                  "jobType": "PRODUCT",
                                  "status": "ACTIVE",
                                  "payload": {
                                    "seller": "PB"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("PB 크롤러"))
                .andReturn();
        long jobId = readDataId(createdJob);

        mockMvc.perform(post("/api/v1/crawler/admin/jobs/" + jobId + "/run")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runId").exists());

        mockMvc.perform(get("/api/v1/crawler/admin/monitoring")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobCount").value(1));
    }

    @Test
    void opsDashboardAndObservabilityWork() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");

        mockMvc.perform(get("/api/v1/admin/ops-dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallStatus").value("degraded"));

        mockMvc.perform(get("/api/v1/admin/observability/metrics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRequests").value(120));

        mockMvc.perform(get("/api/v1/admin/observability/traces?limit=5&pathContains=/api/v1/health")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].path").value("/api/v1/health"));

        mockMvc.perform(get("/api/v1/admin/observability/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.opsSummary.overallStatus").value("degraded"));
    }

    private long readDataId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private Category createCategory(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug + "-" + UUID.randomUUID());
        category.setDepth(0);
        category.setSortOrder(0);
        category.setVisible(true);
        return categoryRepository.save(category);
    }

    private Product createProduct(Category category, String name, String slug) {
        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setSlug(slug + "-" + UUID.randomUUID());
        product.setBrand("PB");
        product.setDescription("417 테스트 상품");
        product.setThumbnailUrl("https://cdn.pbshop.local/" + slug + ".png");
        product.setReviewCount(0);
        product.setRatingAvg(BigDecimal.ZERO);
        product.setStatus("ACTIVE");
        return productRepository.save(product);
    }

    private ProductSpec createProductSpec(Product product, String key, String value) {
        ProductSpec spec = new ProductSpec();
        spec.setProduct(product);
        spec.setSpecKey(key);
        spec.setSpecValue(value);
        spec.setSortOrder(0);
        product.getSpecs().add(spec);
        return productSpecRepository.save(spec);
    }

    private Seller createSeller(String name, String code) {
        Seller seller = new Seller();
        seller.setName(name);
        seller.setCode(code + "-" + UUID.randomUUID());
        seller.setHomepageUrl("https://seller.pbshop.local/" + code);
        seller.setStatus("ACTIVE");
        return sellerRepository.save(seller);
    }

    private PriceEntry createPriceEntry(Product product, Seller seller, BigDecimal price) {
        PriceEntry entry = new PriceEntry();
        entry.setProduct(product);
        entry.setSeller(seller);
        entry.setPrice(price);
        entry.setOriginalPrice(price);
        entry.setShippingFee(BigDecimal.ZERO);
        entry.setStockStatus("IN_STOCK");
        entry.setPurchaseUrl("https://seller.pbshop.local/p/" + product.getSlug());
        product.getPriceEntries().add(entry);
        return priceEntryRepository.save(entry);
    }

    private String createUserAndLogin(String role) throws Exception {
        String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@pbshop.test";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setName(role.toLowerCase() + "-user");
        user.setPhone("01012349876");
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setEmailVerified(true);
        userRepository.save(user);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int tokenStart = response.indexOf("\"accessToken\":\"") + 15;
        int tokenEnd = response.indexOf("\"", tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }
}
