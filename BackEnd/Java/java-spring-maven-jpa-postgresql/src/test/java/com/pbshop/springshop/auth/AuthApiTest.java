package com.pbshop.springshop.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import com.pbshop.springshop.support.ApiIntegrationSupport;
import com.pbshop.springshop.user.UserRepository;

@ActiveProfiles("test")
@Transactional
class AuthApiTest extends ApiIntegrationSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private AuthRefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthPasswordResetRequestRepository passwordResetRequestRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Test
    void signupVerifyLoginRefreshLogoutAndResetPasswordFlow() throws Exception {
        String email = "auth-" + UUID.randomUUID() + "@pbshop.test";
        String phone = "01012341234";

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!",
                                  "name": "auth-user",
                                  "phone": "%s"
                                }
                                """.formatted(email, phone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email));

        AuthVerificationCode verificationCode = verificationCodeRepository
                .findTopByEmailAndPurposeAndConsumedAtIsNullOrderByIdDesc(email, "EMAIL_VERIFY")
                .orElseThrow();

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "%s"
                                }
                                """.formatted(email, verificationCode.getCode())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.path("data").path("accessToken").asText();
        String refreshToken = loginJson.path("data").path("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("로그아웃 되었습니다."));

        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "phone": "%s"
                                }
                                """.formatted(email, phone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        AuthPasswordResetRequest resetRequest = passwordResetRequestRepository
                .findTopByEmailAndConsumedAtIsNullOrderByIdDesc(email)
                .orElseThrow();

        MvcResult verifyResetResult = mockMvc.perform(post("/api/v1/auth/password-reset/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "%s"
                                }
                                """.formatted(email, resetRequest.getCode())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resetToken").exists())
                .andReturn();

        String resetToken = objectMapper.readTree(verifyResetResult.getResponse().getContentAsString())
                .path("data")
                .path("resetToken")
                .asText();

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resetToken": "%s",
                                  "newPassword": "Password456!"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("비밀번호가 변경되었습니다."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password456!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    void socialFlowSupportsCallbackCompleteLinkAndUnlink() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login/google"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/public/login?provider=google")));

        MvcResult callbackResult = mockMvc.perform(get("/api/v1/auth/callback/google").param("code", "oauth-user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andReturn();

        String accessToken = objectMapper.readTree(callbackResult.getResponse().getContentAsString())
                .path("data")
                .path("accessToken")
                .asText();

        mockMvc.perform(post("/api/v1/auth/social/complete")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "01099998888",
                                  "nickname": "google-user"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());

        mockMvc.perform(post("/api/v1/auth/social/link")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "facebook",
                                  "socialToken": "fb-token-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.linkedProvider").value("facebook"));

        mockMvc.perform(delete("/api/v1/auth/social/unlink/facebook")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("소셜 계정 연동이 해제되었습니다."));

        org.assertj.core.api.Assertions.assertThat(socialAccountRepository.findByProviderAndProviderUserId("facebook", "facebook:fb-token-1"))
                .isEmpty();
    }

    @Test
    void protectedAuthEndpointsRequireBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_401"));

        mockMvc.perform(post("/api/v1/auth/social/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "google",
                                  "socialToken": "token-1"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_401"));
    }
}

