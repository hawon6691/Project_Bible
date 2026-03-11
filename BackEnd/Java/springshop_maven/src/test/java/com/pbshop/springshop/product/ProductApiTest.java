package com.pbshop.springshop.product;

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
import com.pbshop.springshop.support.ApiIntegrationSupport;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class ProductApiTest extends ApiIntegrationSupport {

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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void publicCanReadProductListAndDetail() throws Exception {
        Category category = createCategory("노트북", "notebook");
        Product product = createProduct(category, "게이밍 노트북 A15", "gaming-notebook-a15", "PB", "테스트 상품");
        createSpec(product, "cpu", "Ryzen 7", 0);
        createSpec(product, "ram", "32GB", 1);
        Seller seller = createSeller("PB몰", "pbmall");
        createPriceEntry(product, seller, new BigDecimal("1599000"), new BigDecimal("1699000"));

        mockMvc.perform(get("/api/v1/products")
                        .param("categoryId", category.getId().toString())
                        .param("search", "게이밍")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("게이밍 노트북 A15"))
                .andExpect(jsonPath("$.data.items[0].lowestPrice").value(1599000))
                .andExpect(jsonPath("$.data.items[0].sellerCount").value(1));

        mockMvc.perform(get("/api/v1/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("게이밍 노트북 A15"))
                .andExpect(jsonPath("$.data.category.name").value("노트북"))
                .andExpect(jsonPath("$.data.specs[0].key").value("cpu"))
                .andExpect(jsonPath("$.data.priceEntries[0].sellerName").value("PB몰"));
    }

    @Test
    void adminCanCreateUpdateAndDeleteProduct() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        Category category = createCategory("모니터", "monitor");

        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "name": "PB 모니터",
                                  "slug": "pb-monitor",
                                  "brand": "PB",
                                  "description": "상품 설명",
                                  "thumbnailUrl": "https://cdn.pbshop.local/pb-monitor.png",
                                  "reviewCount": 12,
                                  "ratingAvg": 4.5,
                                  "status": "ACTIVE"
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("pb-monitor"))
                .andReturn();

        long productId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "name": "PB 모니터 Pro",
                                  "slug": "pb-monitor-pro",
                                  "brand": "PB",
                                  "description": "업데이트 설명",
                                  "thumbnailUrl": "https://cdn.pbshop.local/pb-monitor-pro.png",
                                  "reviewCount": 20,
                                  "ratingAvg": 4.8,
                                  "status": "ACTIVE"
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("PB 모니터 Pro"))
                .andExpect(jsonPath("$.data.slug").value("pb-monitor-pro"));

        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("상품이 삭제되었습니다."));
    }

    @Test
    void nonAdminCannotManageProducts() throws Exception {
        String userToken = createUserAndLogin("USER");
        Category category = createCategory("키보드", "keyboard");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "name": "차단 상품",
                                  "slug": "blocked-product",
                                  "brand": "PB",
                                  "description": "상품 설명"
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_403"));
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

    private Product createProduct(Category category, String name, String slug, String brand, String description) {
        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setSlug(slug);
        product.setBrand(brand);
        product.setDescription(description);
        product.setThumbnailUrl("https://cdn.pbshop.local/" + slug + ".png");
        product.setReviewCount(5);
        product.setRatingAvg(new BigDecimal("4.30"));
        product.setStatus("ACTIVE");
        return productRepository.save(product);
    }

    private void createSpec(Product product, String key, String value, int sortOrder) {
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
        PriceEntry priceEntry = new PriceEntry();
        priceEntry.setProduct(product);
        priceEntry.setSeller(seller);
        priceEntry.setPrice(price);
        priceEntry.setOriginalPrice(originalPrice);
        priceEntry.setShippingFee(BigDecimal.ZERO);
        priceEntry.setStockStatus("IN_STOCK");
        priceEntry.setPurchaseUrl("https://seller.pbshop.local/p/" + product.getSlug());
        product.getPriceEntries().add(priceEntry);
        priceEntryRepository.save(priceEntry);
    }

    private String createUserAndLogin(String role) throws Exception {
        String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@pbshop.test";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setName(role.toLowerCase() + "-user");
        user.setPhone("01022223333");
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

