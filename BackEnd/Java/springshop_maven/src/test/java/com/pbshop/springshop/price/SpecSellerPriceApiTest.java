package com.pbshop.springshop.price;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

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
import com.pbshop.springshop.spec.SpecDefinition;
import com.pbshop.springshop.spec.SpecDefinitionRepository;
import com.pbshop.springshop.support.ApiIntegrationSupport;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class SpecSellerPriceApiTest extends ApiIntegrationSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSpecRepository productSpecRepository;

    @Autowired
    private PriceEntryRepository priceEntryRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SpecDefinitionRepository specDefinitionRepository;

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void publicCanReadSpecDefinitionsSellerListAndProductPrices() throws Exception {
        Category category = createCategory("노트북", "notebook");
        Product product = createProduct(category, "PB 노트북", "pb-notebook");
        createSpecDefinition(category, "cpu", "CPU", List.of("i5", "i7"), "TEXT");
        createProductSpec(product, "cpu", "i7", 0);
        Seller seller = createSeller("PB몰", "pbmall");
        createPriceEntry(product, seller, new BigDecimal("1500000"), new BigDecimal("1600000"));

        mockMvc.perform(get("/api/v1/specs/definitions").param("categoryId", category.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].key").value("cpu"));

        mockMvc.perform(get("/api/v1/products/" + product.getId() + "/specs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].key").value("cpu"));

        mockMvc.perform(get("/api/v1/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("PB몰"));

        mockMvc.perform(get("/api/v1/products/" + product.getId() + "/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lowestPrice").value(1500000))
                .andExpect(jsonPath("$.data.entries[0].sellerName").value("PB몰"));

        mockMvc.perform(get("/api/v1/products/" + product.getId() + "/price-history").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].price").value(1500000));
    }

    @Test
    void adminCanManageDefinitionsAndSellers() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        Category category = createCategory("모니터", "monitor");

        MvcResult definitionResult = mockMvc.perform(post("/api/v1/specs/definitions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "key": "panel_type",
                                  "name": "패널",
                                  "inputType": "SELECT",
                                  "options": ["IPS", "VA"],
                                  "unit": "",
                                  "sortOrder": 1,
                                  "filterable": true
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.key").value("panel_type"))
                .andReturn();

        long definitionId = objectMapper.readTree(definitionResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/v1/specs/definitions/" + definitionId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "key": "panel_type",
                                  "name": "패널 타입",
                                  "inputType": "SELECT",
                                  "options": ["IPS", "VA", "OLED"],
                                  "unit": "",
                                  "sortOrder": 2,
                                  "filterable": true
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("패널 타입"));

        mockMvc.perform(delete("/api/v1/specs/definitions/" + definitionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("스펙 정의가 삭제되었습니다."));

        MvcResult sellerResult = mockMvc.perform(post("/api/v1/sellers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "PB전자",
                                  "code": "pbelectronics",
                                  "homepageUrl": "https://pbshop.local/pbelectronics",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("pbelectronics"))
                .andReturn();

        long sellerId = objectMapper.readTree(sellerResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/v1/sellers/" + sellerId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "PB전자몰",
                                  "code": "pbelectronics",
                                  "homepageUrl": "https://pbshop.local/pbelectronics",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("PB전자몰"));

        mockMvc.perform(delete("/api/v1/sellers/" + sellerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("판매처가 삭제되었습니다."));
    }

    @Test
    void sellerOrAdminCanManagePricesAndUserCanManageAlerts() throws Exception {
        String sellerToken = createUserAndLogin("SELLER");
        String adminToken = createUserAndLogin("ADMIN");
        String userToken = createUserAndLogin("USER");

        Category category = createCategory("태블릿", "tablet");
        Product product = createProduct(category, "PB 태블릿", "pb-tablet");
        Seller seller = createSeller("PB셀러", "pbseller");

        MvcResult createResult = mockMvc.perform(post("/api/v1/products/" + product.getId() + "/prices")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerId": %d,
                                  "price": 799000,
                                  "originalPrice": 899000,
                                  "shippingFee": 0,
                                  "stockStatus": "IN_STOCK",
                                  "purchaseUrl": "https://seller.pbshop.local/p/%s"
                                }
                                """.formatted(seller.getId(), product.getSlug())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(799000))
                .andReturn();

        long priceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/v1/prices/" + priceId)
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "price": 759000,
                                  "originalPrice": 899000,
                                  "shippingFee": 0,
                                  "stockStatus": "IN_STOCK",
                                  "purchaseUrl": "https://seller.pbshop.local/p/%s"
                                }
                                """.formatted(product.getSlug())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(759000));

        MvcResult alertResult = mockMvc.perform(post("/api/v1/price-alerts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "targetPrice": 700000
                                }
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productName").value("PB 태블릿"))
                .andReturn();

        long alertId = objectMapper.readTree(alertResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/price-alerts")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].targetPrice").value(700000));

        mockMvc.perform(delete("/api/v1/prices/" + priceId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_403"));

        mockMvc.perform(delete("/api/v1/price-alerts/" + alertId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("가격 알림이 삭제되었습니다."));

        mockMvc.perform(delete("/api/v1/prices/" + priceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("가격 정보가 삭제되었습니다."));
    }

    private Category createCategory(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDepth(0);
        category.setSortOrder(0);
        category.setVisible(true);
        return categoryRepository.save(category);
    }

    private Product createProduct(Category category, String name, String slug) {
        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setSlug(slug);
        product.setBrand("PB");
        product.setDescription("테스트 상품");
        product.setThumbnailUrl("https://cdn.pbshop.local/" + slug + ".png");
        product.setReviewCount(0);
        product.setRatingAvg(BigDecimal.ZERO);
        product.setStatus("ACTIVE");
        return productRepository.save(product);
    }

    private SpecDefinition createSpecDefinition(Category category, String key, String name, List<String> options, String inputType)
            throws Exception {
        SpecDefinition definition = new SpecDefinition();
        definition.setCategory(category);
        definition.setSpecKey(key);
        definition.setName(name);
        definition.setInputType(inputType);
        definition.setOptionsJson(objectMapper.writeValueAsString(options));
        definition.setUnit("");
        definition.setSortOrder(0);
        definition.setFilterable(true);
        return specDefinitionRepository.save(definition);
    }

    private void createProductSpec(Product product, String key, String value, int sortOrder) {
        ProductSpec spec = new ProductSpec();
        spec.setProduct(product);
        spec.setSpecKey(key);
        spec.setSpecValue(value);
        spec.setSortOrder(sortOrder);
        product.getSpecs().add(spec);
        productSpecRepository.save(spec);
    }

    private Seller createSeller(String name, String code) {
        Seller seller = new Seller();
        seller.setName(name);
        seller.setCode(code);
        seller.setHomepageUrl("https://seller.pbshop.local/" + code);
        seller.setStatus("ACTIVE");
        return sellerRepository.save(seller);
    }

    private void createPriceEntry(Product product, Seller seller, BigDecimal price, BigDecimal originalPrice) {
        PriceEntry entry = new PriceEntry();
        entry.setProduct(product);
        entry.setSeller(seller);
        entry.setPrice(price);
        entry.setOriginalPrice(originalPrice);
        entry.setShippingFee(BigDecimal.ZERO);
        entry.setStockStatus("IN_STOCK");
        entry.setPurchaseUrl("https://seller.pbshop.local/p/" + product.getSlug());
        product.getPriceEntries().add(entry);
        priceEntryRepository.save(entry);
    }

    private String createUserAndLogin(String role) throws Exception {
        String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@pbshop.test";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setName(role.toLowerCase() + "-user");
        user.setPhone("01033334444");
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setEmailVerified(true);
        userRepository.save(user);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data")
                .path("accessToken")
                .asText();
    }
}

