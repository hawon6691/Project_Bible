package com.pbshop.springshop.category;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import com.pbshop.springshop.system.SystemControllerTestSupport;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class CategoryApiTest extends SystemControllerTestSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void publicCanReadCategoryTreeAndCategoryDetail() throws Exception {
        Category parent = createCategory(null, "컴퓨터", "computer", 0, true);
        Category child = createCategory(parent, "노트북", "notebook", 1, true);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("컴퓨터"))
                .andExpect(jsonPath("$.data[0].children[0].name").value("노트북"));

        mockMvc.perform(get("/api/v1/categories/" + child.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("노트북"))
                .andExpect(jsonPath("$.data.parent.id").value(parent.getId()))
                .andExpect(jsonPath("$.data.parent.name").value("컴퓨터"));
    }

    @Test
    void adminCanCreateUpdateAndDeleteCategory() throws Exception {
        String adminToken = createAdminAndLogin();

        MvcResult createResult = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "가전/TV",
                                  "slug": "home-tv",
                                  "sortOrder": 3,
                                  "isVisible": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("home-tv"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andReturn();

        Long categoryId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(patch("/api/v1/categories/" + categoryId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "가전/TV 수정",
                                  "slug": "home-tv-updated",
                                  "sortOrder": 5,
                                  "isVisible": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("가전/TV 수정"))
                .andExpect(jsonPath("$.data.slug").value("home-tv-updated"))
                .andExpect(jsonPath("$.data.isVisible").value(false));

        mockMvc.perform(delete("/api/v1/categories/" + categoryId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("카테고리가 삭제되었습니다."));
    }

    @Test
    void nonAdminIsBlockedAndCategoryWithChildrenCannotBeDeleted() throws Exception {
        Category parent = createCategory(null, "컴퓨터", "computer", 0, true);
        createCategory(parent, "데스크탑", "desktop", 1, true);

        String userToken = createUserAndLogin();
        String adminToken = createAdminAndLogin();

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "차단 테스트",
                                  "sortOrder": 1,
                                  "isVisible": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_403"));

        mockMvc.perform(delete("/api/v1/categories/" + parent.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    private Category createCategory(Category parent, String name, String slug, int sortOrder, boolean isVisible) {
        Category category = new Category();
        category.setParent(parent);
        category.setName(name);
        category.setSlug(slug);
        category.setDepth(parent == null ? 0 : parent.getDepth() + 1);
        category.setSortOrder(sortOrder);
        category.setVisible(isVisible);
        return categoryRepository.save(category);
    }

    private String createAdminAndLogin() throws Exception {
        return createUserAndLogin("ADMIN");
    }

    private String createUserAndLogin() throws Exception {
        return createUserAndLogin("USER");
    }

    private String createUserAndLogin(String role) throws Exception {
        String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@pbshop.test";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setName(role.toLowerCase() + "-user");
        user.setPhone("01012345678");
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
