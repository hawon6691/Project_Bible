package com.pbshop.springshop.fraud;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import com.pbshop.springshop.category.Category;
import com.pbshop.springshop.category.CategoryRepository;
import com.pbshop.springshop.product.PriceEntry;
import com.pbshop.springshop.product.PriceEntryRepository;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.product.SellerRepository;
import com.pbshop.springshop.support.ApiIntegrationSupport;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class FraudTrustI18nImageBadgeApiTest extends ApiIntegrationSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PriceEntryRepository priceEntryRepository;

    @Test
    void fraudAndTrustFlowWorks() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        Category category = createCategory("그래픽카드", "gpu");
        Product product = createProduct(category, "PB GPU", "pb-gpu");
        Seller seller = createSeller("PB 마켓", "pb-market");
        createPriceEntry(product, seller, new BigDecimal("1000000"), new BigDecimal("0"));
        PriceEntry anomaly = createPriceEntry(product, seller, new BigDecimal("500000"), new BigDecimal("2500"));

        mockMvc.perform(get("/api/v1/products/" + product.getId() + "/real-price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPrice").value(502500.00));

        mockMvc.perform(get("/api/v1/fraud/products/" + product.getId() + "/effective-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerId").value(seller.getId()));

        mockMvc.perform(get("/api/v1/fraud/products/" + product.getId() + "/anomalies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].priceEntryId").value(anomaly.getId()));

        mockMvc.perform(post("/api/v1/fraud/admin/products/" + product.getId() + "/scan")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].reason").value("평균 가격 대비 급격히 낮은 가격이 감지되었습니다."));

        MvcResult alerts = mockMvc.perform(get("/api/v1/fraud/alerts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andReturn();

        long flagId = readDataArrayFirstId(alerts);

        mockMvc.perform(patch("/api/v1/fraud/alerts/" + flagId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("이상 가격 알림이 승인되었습니다."));

        mockMvc.perform(get("/api/v1/fraud/admin/products/" + product.getId() + "/flags")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/trust/sellers/" + seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sellerId").value(seller.getId()))
                .andExpect(jsonPath("$.data.breakdown.deliveryScore").exists());

        mockMvc.perform(post("/api/v1/trust/admin/sellers/" + seller.getId() + "/recalculate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grade").isNotEmpty());

        mockMvc.perform(get("/api/v1/trust/sellers/" + seller.getId() + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerId").value(seller.getId()));
    }

    @Test
    void i18nAndImageFlowWorks() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        String userToken = createUserAndLogin("USER");

        MvcResult translation = mockMvc.perform(post("/api/v1/i18n/admin/translations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locale": "ko",
                                  "namespace": "product",
                                  "key": "buy_now",
                                  "value": "바로 구매"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.key").value("buy_now"))
                .andReturn();

        long translationId = readDataId(translation);

        mockMvc.perform(get("/api/v1/i18n/translations")
                        .param("locale", "ko")
                        .param("namespace", "product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].value").value("바로 구매"));

        mockMvc.perform(post("/api/v1/i18n/admin/exchange-rates")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "baseCurrency": "KRW",
                                  "targetCurrency": "USD",
                                  "rate": 0.00075
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.baseCurrency").value("KRW"));

        mockMvc.perform(get("/api/v1/i18n/convert")
                        .param("amount", "10000")
                        .param("from", "KRW")
                        .param("to", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.convertedAmount").value(7.50));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-binary".getBytes()
        );

        MvcResult upload = mockMvc.perform(multipart("/api/v1/images/upload")
                        .file(file)
                        .param("category", "PRODUCT")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.variants.length()").value(3))
                .andReturn();

        long imageId = readDataId(upload);

        mockMvc.perform(get("/api/v1/images/" + imageId + "/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("THUMBNAIL"));

        mockMvc.perform(delete("/api/v1/images/" + imageId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("이미지가 삭제되었습니다."));

        mockMvc.perform(delete("/api/v1/i18n/admin/translations/" + translationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("번역이 삭제되었습니다."));
    }

    @Test
    void badgeFlowWorks() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        String userToken = createUserAndLogin("USER");
        User user = findUserByRole("USER");

        MvcResult createdBadge = mockMvc.perform(post("/api/v1/admin/badges")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "신규 회원",
                                  "description": "첫 가입 배지",
                                  "iconUrl": "https://cdn.pbshop.local/badges/newbie.png",
                                  "type": "MANUAL",
                                  "condition": {
                                    "signupDays": 1
                                  },
                                  "rarity": "COMMON"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("신규 회원"))
                .andReturn();

        long badgeId = readDataId(createdBadge);

        mockMvc.perform(post("/api/v1/admin/badges/" + badgeId + "/grant")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(user.getId()));

        mockMvc.perform(get("/api/v1/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].holderCount").value(1));

        mockMvc.perform(get("/api/v1/badges/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].badge.name").value("신규 회원"));

        mockMvc.perform(get("/api/v1/users/" + user.getId() + "/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].badge.id").value(badgeId));

        mockMvc.perform(delete("/api/v1/admin/badges/" + badgeId + "/revoke/" + user.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("배지가 회수되었습니다."));

        mockMvc.perform(delete("/api/v1/admin/badges/" + badgeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("배지가 삭제되었습니다."));
    }

    private long readDataId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private long readDataArrayFirstId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").get(0).path("id").asLong();
    }

    private User findUserByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equalsIgnoreCase(user.getRole()))
                .findFirst()
                .orElseThrow();
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
        product.setDescription("407 테스트 상품");
        product.setThumbnailUrl("https://cdn.pbshop.local/" + slug + ".png");
        product.setReviewCount(0);
        product.setRatingAvg(BigDecimal.ZERO);
        product.setStatus("ACTIVE");
        return productRepository.save(product);
    }

    private Seller createSeller(String name, String code) {
        Seller seller = new Seller();
        seller.setName(name);
        seller.setCode(code);
        seller.setHomepageUrl("https://seller.pbshop.local/" + code);
        seller.setStatus("ACTIVE");
        return sellerRepository.save(seller);
    }

    private PriceEntry createPriceEntry(Product product, Seller seller, BigDecimal price, BigDecimal shippingFee) {
        PriceEntry entry = new PriceEntry();
        entry.setProduct(product);
        entry.setSeller(seller);
        entry.setPrice(price);
        entry.setOriginalPrice(price);
        entry.setShippingFee(shippingFee);
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
