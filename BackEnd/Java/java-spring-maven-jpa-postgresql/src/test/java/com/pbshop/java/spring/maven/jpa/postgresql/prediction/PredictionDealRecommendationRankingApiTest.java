package com.pbshop.java.spring.maven.jpa.postgresql.prediction;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import com.pbshop.java.spring.maven.jpa.postgresql.activity.RecentProductView;
import com.pbshop.java.spring.maven.jpa.postgresql.activity.RecentProductViewRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.activity.SearchHistory;
import com.pbshop.java.spring.maven.jpa.postgresql.activity.SearchHistoryRepository;
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
class PredictionDealRecommendationRankingApiTest extends ApiIntegrationSupport {

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

    @Autowired
    private RecentProductViewRepository recentProductViewRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Test
    void predictionAndDealFlowWorks() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        Category category = createCategory("노트북", "laptop");
        Product product = createProduct(category, "PB 노트북", "pb-laptop");
        Seller seller = createSeller("PB 스토어", "pb-store");
        createPriceEntry(product, seller, new BigDecimal("1590000"));
        createPriceEntry(product, seller, new BigDecimal("1550000"));
        OffsetDateTime dealStartAt = OffsetDateTime.now().minusDays(1).withNano(0);
        OffsetDateTime dealEndAt = OffsetDateTime.now().plusDays(1).withNano(0);

        mockMvc.perform(get("/api/v1/predictions/products/" + product.getId() + "/price-trend")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(product.getId()))
                .andExpect(jsonPath("$.data.predictions.length()").value(3));

        MvcResult createdDeal = mockMvc.perform(post("/api/v1/deals/admin")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "title": "노트북 특가",
                                  "type": "SPECIAL",
                                  "description": "주말 특가",
                                  "dealPrice": 1490000,
                                  "discountRate": 10,
                                  "stock": 20,
                                  "bannerUrl": "/deals/pb-laptop.png",
                                  "startAt": "%s",
                                  "endAt": "%s"
                                }
                                """.formatted(product.getId(), dealStartAt, dealEndAt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.product.id").value(product.getId()))
                .andExpect(jsonPath("$.data.title").value("노트북 특가"))
                .andReturn();

        long dealId = objectMapper.readTree(createdDeal.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].product.id").value(product.getId()));

        mockMvc.perform(patch("/api/v1/deals/admin/" + dealId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "discountRate": 15,
                                  "stock": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.discountRate").value(15));

        mockMvc.perform(delete("/api/v1/deals/admin/" + dealId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("특가 정보가 삭제되었습니다."));
    }

    @Test
    void recommendationAndRankingFlowWorks() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        String userToken = createUserAndLogin("USER");
        User user = findUserByRole("USER");
        Category category = createCategory("키보드", "keyboard");
        Product product = createProduct(category, "PB 키보드", "pb-keyboard");
        Seller seller = createSeller("PB 셀러", "pb-seller");
        createPriceEntry(product, seller, new BigDecimal("99000"));
        createRecentView(user, product);
        createSearch(user, "기계식 키보드");

        mockMvc.perform(post("/api/v1/admin/recommendations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "targetType": "TRENDING",
                                  "reason": "가성비 추천",
                                  "score": 95.5
                                }
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.product.id").value(product.getId()));

        mockMvc.perform(get("/api/v1/recommendations/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source").value("TRENDING"))
                .andExpect(jsonPath("$.data.items[0].product.id").value(product.getId()));

        mockMvc.perform(get("/api/v1/recommendations/personal")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].product.id").value(product.getId()));

        mockMvc.perform(get("/api/v1/rankings/products/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].product.id").value(product.getId()));

        mockMvc.perform(get("/api/v1/rankings/keywords/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("기계식 키보드"));

        mockMvc.perform(post("/api/v1/rankings/admin/recalculate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedCount").value(2));
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
        product.setDescription("추천/랭킹 테스트 상품");
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

    private void createRecentView(User user, Product product) {
        RecentProductView view = new RecentProductView();
        view.setUser(user);
        view.setProduct(product);
        recentProductViewRepository.save(view);
    }

    private void createSearch(User user, String keyword) {
        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setKeyword(keyword);
        searchHistoryRepository.save(history);
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
