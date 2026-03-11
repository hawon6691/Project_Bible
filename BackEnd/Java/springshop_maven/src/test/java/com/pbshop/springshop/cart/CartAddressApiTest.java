package com.pbshop.springshop.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.pbshop.springshop.category.Category;
import com.pbshop.springshop.category.CategoryRepository;
import com.pbshop.springshop.product.PriceEntry;
import com.pbshop.springshop.product.PriceEntryRepository;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.product.SellerRepository;
import com.pbshop.springshop.support.ApiIntegrationTestSupport;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class CartAddressApiTest extends ApiIntegrationTestSupport {

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
    private PasswordEncoder passwordEncoder;

    @Test
    void userCanManageAddresses() throws Exception {
        String token = createUserAndLogin("USER");

        MvcResult createResult = mockMvc.perform(post("/api/v1/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientName": "홍길동",
                                  "phone": "01011112222",
                                  "zipCode": "12345",
                                  "address1": "서울시 강남구",
                                  "address2": "101동 1001호",
                                  "label": "집",
                                  "deliveryRequest": "문 앞",
                                  "isDefault": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDefault").value(true))
                .andReturn();

        long addressId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/addresses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].recipientName").value("홍길동"));

        mockMvc.perform(patch("/api/v1/addresses/" + addressId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientName": "홍길동",
                                  "phone": "01011112222",
                                  "zipCode": "12345",
                                  "address1": "서울시 서초구",
                                  "address2": "202동 202호",
                                  "label": "회사",
                                  "deliveryRequest": "경비실",
                                  "isDefault": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.address1").value("서울시 서초구"));

        mockMvc.perform(delete("/api/v1/addresses/" + addressId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("배송지가 삭제되었습니다."));
    }

    @Test
    void userCanManageCart() throws Exception {
        String token = createUserAndLogin("USER");
        Category category = createCategory("노트북", "notebook");
        Product product = createProduct(category, "PB 노트북", "pb-notebook");
        Seller seller = createSeller("PB몰", "pbmall");
        createPriceEntry(product, seller, new BigDecimal("1500000"));

        MvcResult addResult = mockMvc.perform(post("/api/v1/cart")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "sellerId": %d,
                                  "quantity": 1,
                                  "selectedOptions": ["블랙", "32GB"]
                                }
                                """.formatted(product.getId(), seller.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(1))
                .andReturn();

        long itemId = objectMapper.readTree(addResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/cart")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "sellerId": %d,
                                  "quantity": 2,
                                  "selectedOptions": ["블랙", "32GB"]
                                }
                                """.formatted(product.getId(), seller.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(3));

        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(3))
                .andExpect(jsonPath("$.data.summary.totalAmount").value(4500000));

        mockMvc.perform(patch("/api/v1/cart/" + itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(5));

        mockMvc.perform(delete("/api/v1/cart/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("장바구니 항목이 삭제되었습니다."));

        mockMvc.perform(post("/api/v1/cart")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "sellerId": %d,
                                  "quantity": 1,
                                  "selectedOptions": []
                                }
                                """.formatted(product.getId(), seller.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("장바구니가 비워졌습니다."));
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

