package com.pbshop.springshop.analytics;

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

import com.pbshop.springshop.automotive.AutoLeaseOffer;
import com.pbshop.springshop.automotive.AutoLeaseOfferRepository;
import com.pbshop.springshop.automotive.AutoModel;
import com.pbshop.springshop.automotive.AutoModelRepository;
import com.pbshop.springshop.automotive.AutoOption;
import com.pbshop.springshop.automotive.AutoOptionRepository;
import com.pbshop.springshop.automotive.AutoTrim;
import com.pbshop.springshop.automotive.AutoTrimRepository;
import com.pbshop.springshop.category.Category;
import com.pbshop.springshop.category.CategoryRepository;
import com.pbshop.springshop.pcbuilder.PcBuild;
import com.pbshop.springshop.pcbuilder.PcBuildPart;
import com.pbshop.springshop.pcbuilder.PcBuildPartRepository;
import com.pbshop.springshop.pcbuilder.PcBuildRepository;
import com.pbshop.springshop.product.PriceEntry;
import com.pbshop.springshop.product.PriceEntryRepository;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.product.SellerRepository;
import com.pbshop.springshop.support.ApiIntegrationSupport;
import com.pbshop.springshop.usedmarket.UsedMarketPrice;
import com.pbshop.springshop.usedmarket.UsedMarketPriceRepository;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class AnalyticsUsedMarketAutoAuctionCompareApiTest extends ApiIntegrationSupport {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private PriceEntryRepository priceEntryRepository;
    @Autowired private PcBuildRepository pcBuildRepository;
    @Autowired private PcBuildPartRepository pcBuildPartRepository;
    @Autowired private UsedMarketPriceRepository usedMarketPriceRepository;
    @Autowired private AutoModelRepository autoModelRepository;
    @Autowired private AutoTrimRepository autoTrimRepository;
    @Autowired private AutoOptionRepository autoOptionRepository;
    @Autowired private AutoLeaseOfferRepository autoLeaseOfferRepository;

    @Test
    void analyticsUsedMarketAndAutoFlowWorks() throws Exception {
        String userToken = createUserAndLogin("USER");
        User user = latestUser();
        Category category = createCategory("저장장치", "storage");
        Product product = createProduct(category, "PB SSD", "pb-ssd");
        Seller seller = createSeller("PB Seller", "pb-seller");
        createPriceEntry(product, seller, new BigDecimal("150000"));
        createPriceEntry(product, seller, new BigDecimal("140000"));
        createUsedMarketPrice(product, new BigDecimal("90000"));
        createUsedMarketPrice(product, new BigDecimal("95000"));

        PcBuild build = new PcBuild();
        build.setUser(user);
        build.setName("중고 견적");
        pcBuildRepository.save(build);

        PcBuildPart buildPart = new PcBuildPart();
        buildPart.setPcBuild(build);
        buildPart.setPartType("SSD");
        buildPart.setProduct(product);
        buildPart.setQuantity(1);
        pcBuildPartRepository.save(buildPart);

        AutoModel model = new AutoModel();
        model.setBrand("PB Motors");
        model.setName("PB E-Car");
        model.setType("EV");
        autoModelRepository.save(model);

        AutoTrim trim = new AutoTrim();
        trim.setAutoModel(model);
        trim.setName("Long Range");
        trim.setBasePrice(new BigDecimal("52000000"));
        autoTrimRepository.save(trim);

        AutoOption option = new AutoOption();
        option.setAutoTrim(trim);
        option.setName("HUD");
        option.setPrice(new BigDecimal("1200000"));
        autoOptionRepository.save(option);

        AutoLeaseOffer offer = new AutoLeaseOffer();
        offer.setAutoModel(model);
        offer.setProvider("PB Lease");
        offer.setMonthlyPayment(new BigDecimal("590000"));
        offer.setContractMonths(48);
        autoLeaseOfferRepository.save(offer);

        mockMvc.perform(get("/api/v1/analytics/products/" + product.getId() + "/lowest-ever"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lowestPrice").value(140000.00));

        mockMvc.perform(get("/api/v1/used-market/products/" + product.getId() + "/price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averagePrice").value(92500.00));

        mockMvc.perform(post("/api/v1/used-market/pc-builds/" + build.getId() + "/estimate")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estimatedPrice").value(90000.00))
                .andExpect(jsonPath("$.data.partBreakdown[0].partType").value("SSD"));

        mockMvc.perform(get("/api/v1/auto/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("PB E-Car"));

        mockMvc.perform(post("/api/v1/auto/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "modelId": %d,
                                  "trimId": %d,
                                  "optionIds": [%d]
                                }
                                """.formatted(model.getId(), trim.getId(), option.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.optionPrice").value(1200000.00));
    }

    @Test
    void auctionAndCompareFlowWorks() throws Exception {
        String ownerToken = createUserAndLogin("USER");
        User owner = latestUser();
        String bidderToken = createUserAndLogin("USER");
        Category category = createCategory("전자", "electronics");
        Product productA = createProduct(category, "PB Laptop", "pb-laptop");
        Product productB = createProduct(category, "PB Tablet", "pb-tablet");
        Seller seller = createSeller("PB Compare", "pb-compare");
        createPriceEntry(productA, seller, new BigDecimal("1300000"));
        createPriceEntry(productB, seller, new BigDecimal("800000"));

        MvcResult createdAuction = mockMvc.perform(post("/api/v1/auctions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "노트북 구매 요청",
                                  "description": "가성비 견적",
                                  "categoryId": %d,
                                  "specs": {
                                    "cpu": "i7"
                                  },
                                  "budget": 1500000
                                }
                                """.formatted(category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("노트북 구매 요청"))
                .andReturn();
        long auctionId = readDataId(createdAuction);

        MvcResult bidResult = mockMvc.perform(post("/api/v1/auctions/" + auctionId + "/bids")
                        .header("Authorization", "Bearer " + bidderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "price": 1420000,
                                  "description": "3일 내 배송",
                                  "deliveryDays": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(1420000.00))
                .andReturn();
        long bidId = readDataId(bidResult);

        mockMvc.perform(patch("/api/v1/auctions/" + auctionId + "/bids/" + bidId + "/select")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("낙찰을 선택했습니다."));

        mockMvc.perform(get("/api/v1/auctions/" + auctionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(post("/api/v1/compare/add")
                        .header("X-Compare-Key", "test-compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": %d}
                                """.formatted(productA.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/compare/add")
                        .header("X-Compare-Key", "test-compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": %d}
                                """.formatted(productB.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/compare").header("X-Compare-Key", "test-compare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.compareList.length()").value(2));

        mockMvc.perform(get("/api/v1/compare/detail").header("X-Compare-Key", "test-compare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2));

        mockMvc.perform(delete("/api/v1/compare/" + productB.getId()).header("X-Compare-Key", "test-compare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("비교 상품이 삭제되었습니다."));
    }

    private long readDataId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private User latestUser() {
        return userRepository.findAll().stream().reduce((first, second) -> second).orElseThrow();
    }

    private UsedMarketPrice createUsedMarketPrice(Product product, BigDecimal price) {
        UsedMarketPrice item = new UsedMarketPrice();
        item.setProduct(product);
        item.setPrice(price);
        item.setSource("test");
        return usedMarketPriceRepository.save(item);
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
        product.setDescription("411 테스트 상품");
        product.setThumbnailUrl("https://cdn.pbshop.local/" + slug + ".png");
        product.setReviewCount(0);
        product.setRatingAvg(BigDecimal.ZERO);
        product.setStatus("ACTIVE");
        return productRepository.save(product);
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
