package com.pbshop.springshop.point;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import com.pbshop.springshop.address.Address;
import com.pbshop.springshop.address.AddressRepository;
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
class ReviewWishlistPointApiTest extends ApiIntegrationSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PriceEntryRepository priceEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void userCanCreateUpdateDeleteReviewAndEarnPoints() throws Exception {
        String userToken = createUserAndLogin("USER");
        User user = findUserByRole("USER");

        Category category = createCategory("모니터", "monitor");
        Product product = createProduct(category, "PB 모니터", "pb-monitor");
        Seller seller = createSeller("PB디스플레이", "pb-display");
        createPriceEntry(product, seller, new BigDecimal("299000"));
        Address address = createAddress(user, "리뷰유저");
        long orderId = createOrder(userToken, address.getId(), product.getId(), seller.getId());

        MvcResult createResult = mockMvc.perform(post("/api/v1/products/" + product.getId() + "/reviews")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "rating": 5,
                                  "content": "가격 대비 만족도가 높습니다."
                                }
                                """.formatted(orderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
        long reviewId = createJson.path("id").asLong();

        mockMvc.perform(get("/api/v1/products/" + product.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(reviewId));

        mockMvc.perform(get("/api/v1/points/balance")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(500));

        mockMvc.perform(patch("/api/v1/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "content": "실사용 기준으로는 4점입니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4));

        Product refreshed = productRepository.findById(product.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(refreshed.getReviewCount()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(refreshed.getRatingAvg()).isEqualByComparingTo("4.00");

        mockMvc.perform(delete("/api/v1/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("리뷰가 삭제되었습니다."));

        Product afterDelete = productRepository.findById(product.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(afterDelete.getReviewCount()).isEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(afterDelete.getRatingAvg()).isEqualByComparingTo("0.00");
    }

    @Test
    void userCanToggleWishlistAndAdminCanGrantPoints() throws Exception {
        String userToken = createUserAndLogin("USER");
        String adminToken = createUserAndLogin("ADMIN");
        User user = findUserByRole("USER");

        Category category = createCategory("이어폰", "earphone");
        Product product = createProduct(category, "PB 이어폰", "pb-earphone");

        mockMvc.perform(post("/api/v1/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wishlisted").value(true));

        mockMvc.perform(get("/api/v1/wishlist")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].productId").value(product.getId()));

        mockMvc.perform(delete("/api/v1/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("위시리스트에서 제거되었습니다."));

        mockMvc.perform(post("/api/v1/admin/points/grant")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "amount": 1500,
                                  "description": "관리자 보상"
                                }
                                """.formatted(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("ADMIN_GRANT"))
                .andExpect(jsonPath("$.data.amount").value(1500));

        mockMvc.perform(get("/api/v1/points/transactions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].type").value("ADMIN_GRANT"));

        mockMvc.perform(get("/api/v1/points/balance")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(1500));
    }

    private long createOrder(String userToken, Long addressId, Long productId, Long sellerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "addressId": %d,
                                  "items": [
                                    {
                                      "productId": %d,
                                      "sellerId": %d,
                                      "quantity": 1,
                                      "selectedOptions": ["기본"]
                                    }
                                  ],
                                  "fromCart": false,
                                  "pointUsed": 0,
                                  "memo": ""
                                }
                                """.formatted(addressId, productId, sellerId)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
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
        product.setDescription("테스트 상품");
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

    private Address createAddress(User user, String recipientName) {
        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(recipientName);
        address.setPhone("01011112222");
        address.setZipCode("12345");
        address.setAddress1("서울시 강남구");
        address.setAddress2("101동");
        address.setLabel("집");
        address.setDeliveryRequest("문 앞");
        address.setDefaultAddress(true);
        return addressRepository.save(address);
    }

    private String createUserAndLogin(String role) throws Exception {
        String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@pbshop.test";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setName(role.toLowerCase() + "-user");
        user.setPhone("01077778888");
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
