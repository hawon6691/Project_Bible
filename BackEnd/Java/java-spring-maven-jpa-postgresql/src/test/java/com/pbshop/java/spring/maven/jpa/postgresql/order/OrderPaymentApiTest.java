package com.pbshop.java.spring.maven.jpa.postgresql.order;

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

import com.pbshop.java.spring.maven.jpa.postgresql.address.Address;
import com.pbshop.java.spring.maven.jpa.postgresql.address.AddressRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.cart.CartItem;
import com.pbshop.java.spring.maven.jpa.postgresql.cart.CartItemRepository;
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
class OrderPaymentApiTest extends ApiIntegrationSupport {

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
    private CartItemRepository cartItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void userCanCreateReadAndCancelOrderAndAdminCanUpdateStatus() throws Exception {
        String userToken = createUserAndLogin("USER");
        String adminToken = createUserAndLogin("ADMIN");
        User user = findUserByRole("USER");

        Category category = createCategory("노트북", "notebook");
        Product product = createProduct(category, "PB 노트북", "pb-notebook");
        Seller seller = createSeller("PB몰", "pbmall");
        createPriceEntry(product, seller, new BigDecimal("1500000"));
        Address address = createAddress(user, "홍길동");

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "addressId": %d,
                                  "items": [
                                    {
                                      "productId": %d,
                                      "sellerId": %d,
                                      "quantity": 2,
                                      "selectedOptions": ["블랙", "32GB"]
                                    }
                                  ],
                                  "fromCart": false,
                                  "pointUsed": 100000,
                                  "memo": "문 앞에 두세요"
                                }
                                """.formatted(address.getId(), product.getId(), seller.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalAmount").value(3000000))
                .andExpect(jsonPath("$.data.finalAmount").value(2900000))
                .andReturn();

        long orderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(orderId));

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(orderId));

        mockMvc.perform(patch("/api/v1/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void userCanCreatePaymentFromCartAndRefund() throws Exception {
        String userToken = createUserAndLogin("USER");
        User user = findUserByRole("USER");

        Category category = createCategory("태블릿", "tablet");
        Product product = createProduct(category, "PB 태블릿", "pb-tablet");
        Seller seller = createSeller("PB셀러", "pbseller");
        createPriceEntry(product, seller, new BigDecimal("799000"));
        Address address = createAddress(user, "김구매");
        createCartItem(user, product, seller, 1, "[\"실버\"]");

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "addressId": %d,
                                  "fromCart": true,
                                  "pointUsed": 0,
                                  "memo": ""
                                }
                                """.formatted(address.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn();

        long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.itemCount").value(0));

        MvcResult paymentResult = mockMvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "method": "CARD"
                                }
                                """.formatted(orderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andReturn();

        long paymentId = objectMapper.readTree(paymentResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/payments/" + paymentId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.method").value("CARD"));

        mockMvc.perform(post("/api/v1/payments/" + paymentId + "/refund")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REFUNDED"));
    }

    private User findUserByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equalsIgnoreCase(user.getRole()))
                .reduce((first, second) -> second)
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
        address.setPhone("01012341234");
        address.setZipCode("12345");
        address.setAddress1("서울시 강남구");
        address.setAddress2("101동 1001호");
        address.setLabel("집");
        address.setDeliveryRequest("문 앞");
        address.setDefaultAddress(true);
        return addressRepository.save(address);
    }

    private void createCartItem(User user, Product product, Seller seller, int quantity, String optionsJson) {
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setSeller(seller);
        cartItem.setQuantity(quantity);
        cartItem.setSelectedOptionsJson(optionsJson);
        cartItemRepository.save(cartItem);
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
