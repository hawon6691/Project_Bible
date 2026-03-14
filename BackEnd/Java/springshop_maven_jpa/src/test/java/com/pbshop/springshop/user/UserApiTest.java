package com.pbshop.springshop.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import com.pbshop.springshop.auth.AuthVerificationCode;
import com.pbshop.springshop.auth.AuthVerificationCodeRepository;
import com.pbshop.springshop.support.ApiIntegrationSupport;

@ActiveProfiles("test")
@Transactional
class UserApiTest extends ApiIntegrationSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthVerificationCodeRepository verificationCodeRepository;

    @Test
    void meProfileAndDeleteFlowWorks() throws Exception {
        String accessToken = signupAndLogin("user-" + UUID.randomUUID() + "@pbshop.test", "Password123!", "pb-user", "01011112222");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.emailVerified").value(true))
                .andExpect(jsonPath("$.data.name").value("pb-user"));

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "pb-user-updated",
                                  "phone": "01099990000",
                                  "password": "Password456!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("pb-user-updated"))
                .andExpect(jsonPath("$.data.phone").value("01099990000"));

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "pb-nick",
                                  "bio": "hello pbshop"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("pb-nick"))
                .andExpect(jsonPath("$.data.bio").value("hello pbshop"));

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "fake-image".getBytes());
        mockMvc.perform(multipart("/api/v1/users/me/profile-image")
                        .file(file)
                        .header("Authorization", "Bearer " + accessToken)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl").value(org.hamcrest.Matchers.containsString("avatar.png")));

        Long userId = userRepository.findByEmail(userEmailFromTokenLogin(accessToken)).orElseThrow().getId();

        mockMvc.perform(get("/api/v1/users/" + userId + "/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("pb-nick"))
                .andExpect(jsonPath("$.data.bio").value("hello pbshop"));

        mockMvc.perform(delete("/api/v1/users/me/profile-image")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("프로필 이미지가 삭제되었습니다."));

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("회원 탈퇴가 완료되었습니다."));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_401"));
    }

    @Test
    void adminCanListUsersAndUpdateStatus() throws Exception {
        String adminToken = createAdminAndLogin("admin-" + UUID.randomUUID() + "@pbshop.test", "AdminPass123!");
        String memberEmail = "member-" + UUID.randomUUID() + "@pbshop.test";
        signupAndLogin(memberEmail, "Password123!", "member-user", "01033334444");
        User member = userRepository.findByEmail(memberEmail).orElseThrow();

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("limit", "10")
                        .param("search", "member-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].email").value(memberEmail))
                .andExpect(jsonPath("$.data.pagination.page").value(1));

        mockMvc.perform(patch("/api/v1/users/" + member.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "SUSPENDED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test
    void nonAdminCannotAccessAdminUserEndpoints() throws Exception {
        String userToken = signupAndLogin("user-" + UUID.randomUUID() + "@pbshop.test", "Password123!", "non-admin", "01055556666");
        User user = userRepository.findByEmail(userEmailFromTokenLogin(userToken)).orElseThrow();

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_403"));

        mockMvc.perform(patch("/api/v1/users/" + user.getId() + "/status")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_403"));
    }

    private String signupAndLogin(String email, String password, String name, String phone) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "name": "%s",
                                  "phone": "%s"
                                }
                                """.formatted(email, password, name, phone)))
                .andExpect(status().isOk());

        AuthVerificationCode verificationCode = verificationCodeRepository
                .findTopByEmailAndPurposeAndConsumedAtIsNullOrderByIdDesc(email, "EMAIL_VERIFY")
                .orElseThrow();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "%s"
                                }
                                """.formatted(email, verificationCode.getCode())))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }

    private String createAdminAndLogin(String email, String password) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName("admin-user");
        user.setPhone("01077778888");
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");
        user.setEmailVerified(true);
        userRepository.save(user);

        MvcResult loginResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }

    private String userEmailFromTokenLogin(String accessToken) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("data").path("email").asText();
    }
}

