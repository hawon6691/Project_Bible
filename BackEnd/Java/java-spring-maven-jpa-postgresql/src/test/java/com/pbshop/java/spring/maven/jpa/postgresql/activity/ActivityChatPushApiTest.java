package com.pbshop.java.spring.maven.jpa.postgresql.activity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import com.pbshop.java.spring.maven.jpa.postgresql.category.Category;
import com.pbshop.java.spring.maven.jpa.postgresql.category.CategoryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntry;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Seller;
import com.pbshop.java.spring.maven.jpa.postgresql.product.SellerRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.support.ApiIntegrationSupport;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class ActivityChatPushApiTest extends ApiIntegrationSupport {

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
    void activityFlowSupportsRecentProductsAndSearchHistory() throws Exception {
        String userToken = createUserAndLogin("USER");
        Category category = createCategory("키보드", "keyboard");
        Product product = createProduct(category, "PB 키보드", "pb-keyboard");
        Seller seller = createSeller("PB 스토어", "pb-store");
        createPriceEntry(product, seller, new BigDecimal("89000"));

        mockMvc.perform(post("/api/v1/activities/recent-products/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(product.getId()));

        mockMvc.perform(post("/api/v1/activities/searches")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "keyword": "기계식 키보드"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("기계식 키보드"));

        mockMvc.perform(get("/api/v1/activities")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentProductCount").value(1))
                .andExpect(jsonPath("$.data.searchCount").value(1));

        MvcResult searches = mockMvc.perform(get("/api/v1/activities/searches")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("기계식 키보드"))
                .andReturn();

        long searchId = objectMapper.readTree(searches.getResponse().getContentAsString()).path("data").get(0).path("id").asLong();

        mockMvc.perform(delete("/api/v1/activities/searches/" + searchId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("검색 기록이 삭제되었습니다."));

        mockMvc.perform(delete("/api/v1/activities/searches")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("검색 기록이 모두 삭제되었습니다."));
    }

    @Test
    void chatFlowSupportsRoomCreationJoinAndMessages() throws Exception {
        String userToken = createUserAndLogin("USER");
        String adminToken = createUserAndLogin("ADMIN");
        User adminUser = findUserByRole("ADMIN");

        MvcResult roomResult = mockMvc.perform(post("/api/v1/chat/rooms")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "1:1 상담",
                                  "participantUserIds": [%d]
                                }
                                """.formatted(adminUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomType").value("DIRECT"))
                .andReturn();

        long roomId = objectMapper.readTree(roomResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/chat/rooms/" + roomId + "/join")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.joined").value(true));

        mockMvc.perform(post("/api/v1/chat/rooms/" + roomId + "/messages")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "문의드립니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomId").value(roomId));

        mockMvc.perform(get("/api/v1/chat/rooms")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(roomId));

        mockMvc.perform(get("/api/v1/chat/rooms/" + roomId + "/messages")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("문의드립니다."));
    }

    @Test
    void pushFlowSupportsSubscriptionAndPreferenceUpdate() throws Exception {
        String userToken = createUserAndLogin("USER");

        mockMvc.perform(post("/api/v1/push/subscriptions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endpoint": "https://push.pbshop.local/subscription/1",
                                  "p256dh": "p256dh-value",
                                  "auth": "auth-value",
                                  "vapidPublicKey": "vapid-key"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/push/subscriptions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].endpoint").value("https://push.pbshop.local/subscription/1"));

        mockMvc.perform(get("/api/v1/push/preferences")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marketingEnabled").value(true));

        mockMvc.perform(post("/api/v1/push/preferences")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "marketingEnabled": false,
                                  "orderEnabled": true,
                                  "chatEnabled": false,
                                  "dealEnabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marketingEnabled").value(false))
                .andExpect(jsonPath("$.data.chatEnabled").value(false));

        mockMvc.perform(post("/api/v1/push/subscriptions/unsubscribe")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endpoint": "https://push.pbshop.local/subscription/1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
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
        product.setDescription("활동 테스트 상품");
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

    private void createPriceEntry(Product product, Seller seller, BigDecimal price) {
        PriceEntry entry = new PriceEntry();
        entry.setProduct(product);
        entry.setSeller(seller);
        entry.setPrice(price);
        entry.setOriginalPrice(price);
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
        user.setPhone("01012349876");
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
