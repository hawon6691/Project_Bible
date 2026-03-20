package com.pbshop.java.spring.maven.jpa.postgresql.community;

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

import com.pbshop.java.spring.maven.jpa.postgresql.category.Category;
import com.pbshop.java.spring.maven.jpa.postgresql.category.CategoryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.inquiry.ProductInquiryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntry;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Seller;
import com.pbshop.java.spring.maven.jpa.postgresql.product.SellerRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.support.ApiIntegrationSupport;
import com.pbshop.java.spring.maven.jpa.postgresql.support.SupportTicketRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class CommunityInquirySupportApiTest extends ApiIntegrationSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PriceEntryRepository priceEntryRepository;

    @Autowired
    private ProductInquiryRepository productInquiryRepository;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Test
    void userCanCreateAndManageCommunityPostFlow() throws Exception {
        String userToken = createUserAndLogin("USER");
        Board board = createBoard("자유게시판", "free-board");

        MvcResult createPostResult = mockMvc.perform(post("/api/v1/boards/" + board.getId() + "/posts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "첫 게시글",
                                  "content": "커뮤니티 API 테스트"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boardId").value(board.getId()))
                .andReturn();

        long postId = objectMapper.readTree(createPostResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/api/v1/boards/" + board.getId() + "/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(postId));

        mockMvc.perform(post("/api/v1/posts/" + postId + "/like")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        MvcResult commentResult = mockMvc.perform(post("/api/v1/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "첫 댓글"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andReturn();

        long commentId = objectMapper.readTree(commentResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/api/v1/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.comments[0].id").value(commentId));

        mockMvc.perform(delete("/api/v1/comments/" + commentId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("댓글이 삭제되었습니다."));

        mockMvc.perform(delete("/api/v1/posts/" + postId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("게시글이 삭제되었습니다."));
    }

    @Test
    void inquiryFlowSupportsSecretMaskingAndSellerAnswer() throws Exception {
        String userToken = createUserAndLogin("USER");
        String sellerToken = createUserAndLogin("SELLER");
        Category category = createCategory("노트북", "laptop");
        Product product = createProduct(category, "PB 노트북", "pb-laptop");
        Seller seller = createSeller("PB셀러", "pb-seller");
        createPriceEntry(product, seller, new BigDecimal("1299000"));

        MvcResult inquiryResult = mockMvc.perform(post("/api/v1/products/" + product.getId() + "/inquiries")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "재고 문의",
                                  "content": "오늘 출고 가능한가요?",
                                  "isSecret": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("재고 문의"))
                .andReturn();

        long inquiryId = objectMapper.readTree(inquiryResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/api/v1/products/" + product.getId() + "/inquiries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("비밀 문의입니다."));

        mockMvc.perform(post("/api/v1/inquiries/" + inquiryId + "/answer")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answer": "오늘 바로 출고 가능합니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ANSWERED"))
                .andExpect(jsonPath("$.data.answer").value("오늘 바로 출고 가능합니다."));

        mockMvc.perform(get("/api/v1/inquiries/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(inquiryId))
                .andExpect(jsonPath("$.data[0].content").value("오늘 출고 가능한가요?"));

        mockMvc.perform(delete("/api/v1/inquiries/" + inquiryId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("문의가 삭제되었습니다."));
    }

    @Test
    void supportFlowSupportsUserTicketAndAdminManagement() throws Exception {
        String userToken = createUserAndLogin("USER");
        String adminToken = createUserAndLogin("ADMIN");

        MvcResult ticketResult = mockMvc.perform(post("/api/v1/support/tickets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "배송",
                                  "title": "배송 상태 확인",
                                  "content": "주문 건 배송 상태를 알고 싶습니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();

        long ticketId = objectMapper.readTree(ticketResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(post("/api/v1/support/tickets/" + ticketId + "/reply")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "배송팀에 확인 후 안내드리겠습니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adminReply").value(true));

        mockMvc.perform(get("/api/v1/support/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ticketId))
                .andExpect(jsonPath("$.data.replies[0].adminReply").value(true));

        mockMvc.perform(get("/api/v1/admin/support/tickets")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(ticketId));

        mockMvc.perform(patch("/api/v1/admin/support/tickets/" + ticketId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RESOLVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    private Board createBoard(String name, String slug) {
        Board board = new Board();
        board.setName(name);
        board.setSlug(slug);
        board.setDescription(name + " 설명");
        board.setActive(true);
        return boardRepository.save(board);
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
        product.setDescription("커뮤니티 테스트 상품");
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
        user.setPhone("01055556666");
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
