package com.pbshop.springshop.pcbuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.pbshop.springshop.matching.ProductMapping;
import com.pbshop.springshop.matching.ProductMappingRepository;
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
class PcFriendShortformMediaNewsMatchingApiTest extends ApiIntegrationSupport {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private PriceEntryRepository priceEntryRepository;
    @Autowired private ProductMappingRepository productMappingRepository;

    @Test
    void pcBuilderAndFriendFlowWorks() throws Exception {
        String userToken = createUserAndLogin("USER");
        String friendToken = createUserAndLogin("USER");
        User friend = latestUser();
        String adminToken = createUserAndLogin("ADMIN");
        Category category = createCategory("CPU", "cpu");
        Product product = createProduct(category, "PB CPU", "pb-cpu");
        createPriceEntry(product, createSeller("PB", "pb"), new BigDecimal("300000"));

        MvcResult build = mockMvc.perform(post("/api/v1/pc-builds")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"내 견적","description":"게임용"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("내 견적"))
                .andReturn();
        long buildId = dataId(build);

        mockMvc.perform(post("/api/v1/pc-builds/" + buildId + "/parts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partType":"CPU","productId":%d,"quantity":1}
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parts[0].product.id").value(product.getId()));

        mockMvc.perform(get("/api/v1/pc-builds/" + buildId + "/share")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shareCode").isNotEmpty());

        mockMvc.perform(post("/api/v1/admin/compatibility-rules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"카테고리 규칙","sourcePartType":"CPU","targetPartType":"CPU","ruleType":"CATEGORY_MISMATCH","ruleValue":{"sameCategory":true}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("카테고리 규칙"));

        mockMvc.perform(post("/api/v1/friends/requests")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d}
                                """.formatted(friend.getId())))
                .andExpect(status().isOk());

        MvcResult received = mockMvc.perform(get("/api/v1/friends/requests/received")
                        .header("Authorization", "Bearer " + friendToken))
                .andExpect(status().isOk())
                .andReturn();
        long friendshipId = objectMapper.readTree(received.getResponse().getContentAsString()).path("data").path("items").get(0).path("id").asLong();

        mockMvc.perform(post("/api/v1/friends/requests/" + friendshipId + "/accept")
                        .header("Authorization", "Bearer " + friendToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("친구 요청을 수락했습니다."));

        mockMvc.perform(get("/api/v1/friends")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].status").value("ACCEPTED"));
    }

    @Test
    void shortformAndMediaFlowWorks() throws Exception {
        String userToken = createUserAndLogin("USER");
        Category category = createCategory("GPU", "gpu");
        Product product = createProduct(category, "PB GPU", "pb-gpu");

        MvcResult created = mockMvc.perform(post("/api/v1/shortforms")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"신상 소개","videoUrl":"https://cdn.pbshop.local/video.mp4","thumbnailUrl":"https://cdn.pbshop.local/thumb.png","productIds":[%d]}
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("신상 소개"))
                .andReturn();
        long shortformId = dataId(created);

        mockMvc.perform(post("/api/v1/shortforms/" + shortformId + "/likes/toggle")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));

        mockMvc.perform(post("/api/v1/shortforms/" + shortformId + "/comments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"좋네요"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("좋네요"));

        mockMvc.perform(post("/api/v1/media/upload")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerType":"SHORTFORM","ownerId":%d,"files":[{"fileName":"clip.mp4","fileUrl":"https://cdn.pbshop.local/clip.mp4","mimeType":"video/mp4","size":1024}]}
                                """.formatted(shortformId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fileName").value("clip.mp4"));

        mockMvc.perform(get("/api/v1/media/presigned-url").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void newsAndMatchingFlowWorks() throws Exception {
        String adminToken = createUserAndLogin("ADMIN");
        Category category = createCategory("SSD", "ssd");
        Product product = createProduct(category, "PB SSD", "pb-ssd");

        MvcResult newsCategory = mockMvc.perform(post("/api/v1/news/admin/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"공지","slug":"notice"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long categoryId = dataId(newsCategory);

        MvcResult news = mockMvc.perform(post("/api/v1/news/admin")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"title":"신제품 출시","content":"출시 안내","thumbnailUrl":"https://cdn.pbshop.local/news.png","productIds":[%d]}
                                """.formatted(categoryId, product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("신제품 출시"))
                .andReturn();
        long newsId = dataId(news);

        ProductMapping mapping = new ProductMapping();
        mapping.setSourceName("PB SSD");
        mapping.setStatus("PENDING");
        productMappingRepository.save(mapping);

        mockMvc.perform(get("/api/v1/matching/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].status").value("PENDING"));

        mockMvc.perform(post("/api/v1/matching/" + mapping.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d}
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("매핑을 승인했습니다."));

        mockMvc.perform(get("/api/v1/news/" + newsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.products[0].id").value(product.getId()));
    }

    private long dataId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private User latestUser() {
        return userRepository.findAll().stream().reduce((first, second) -> second).orElseThrow();
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
        product.setDescription("409 테스트 상품");
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
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password123!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int tokenStart = response.indexOf("\"accessToken\":\"") + 15;
        int tokenEnd = response.indexOf("\"", tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }
}
