package com.pbshop.springshop.support;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Transactional
public abstract class AuthenticatedApiIntegrationSupport extends ApiIntegrationSupport {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected ProductSpecRepository productSpecRepository;

    @Autowired
    protected SellerRepository sellerRepository;

    @Autowired
    protected PriceEntryRepository priceEntryRepository;

    protected String createUserAndLogin(String role) throws Exception {
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

    protected long readDataId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    protected Category createCategory(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug + "-" + UUID.randomUUID());
        category.setDepth(0);
        category.setSortOrder(0);
        category.setVisible(true);
        return categoryRepository.save(category);
    }

    protected Product createProduct(Category category, String name, String slug) {
        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setSlug(slug + "-" + UUID.randomUUID());
        product.setBrand("PB");
        product.setDescription("Java Maven JPA E2E 상품");
        product.setThumbnailUrl("https://cdn.pbshop.local/" + slug + ".png");
        product.setReviewCount(0);
        product.setRatingAvg(BigDecimal.ZERO);
        product.setStatus("ACTIVE");
        return productRepository.save(product);
    }

    protected ProductSpec createProductSpec(Product product, String key, String value) {
        ProductSpec spec = new ProductSpec();
        spec.setProduct(product);
        spec.setSpecKey(key);
        spec.setSpecValue(value);
        spec.setSortOrder(0);
        product.getSpecs().add(spec);
        return productSpecRepository.save(spec);
    }

    protected Seller createSeller(String name, String code) {
        Seller seller = new Seller();
        seller.setName(name);
        seller.setCode(code + "-" + UUID.randomUUID());
        seller.setHomepageUrl("https://seller.pbshop.local/" + code);
        seller.setStatus("ACTIVE");
        return sellerRepository.save(seller);
    }

    protected PriceEntry createPriceEntry(Product product, Seller seller, BigDecimal price) {
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
}
