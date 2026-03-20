package com.pbshop.java.spring.maven.jpa.postgresql.push;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.push.dto.PushDtos.RegisterSubscriptionRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.push.dto.PushDtos.UnregisterSubscriptionRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.push.dto.PushDtos.UpdatePreferenceRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class PushService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushPreferenceRepository pushPreferenceRepository;
    private final UserRepository userRepository;

    public PushService(
            PushSubscriptionRepository pushSubscriptionRepository,
            PushPreferenceRepository pushPreferenceRepository,
            UserRepository userRepository
    ) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.pushPreferenceRepository = pushPreferenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Object> register(
            AuthenticatedUserPrincipal principal,
            RegisterSubscriptionRequest request
    ) {
        String endpointHash = sha256(request.endpoint());
        PushSubscription subscription = pushSubscriptionRepository.findByUserIdAndEndpointHash(principal.userId(), endpointHash)
                .orElseGet(PushSubscription::new);
        subscription.setUser(getUser(principal.userId()));
        subscription.setEndpoint(request.endpoint());
        subscription.setEndpointHash(endpointHash);
        subscription.setP256dh(request.p256dh());
        subscription.setAuth(request.auth());
        subscription.setVapidPublicKey(request.vapidPublicKey());
        subscription.setStatus("ACTIVE");
        return toSubscriptionResponse(pushSubscriptionRepository.save(subscription));
    }

    @Transactional
    public Map<String, Object> unregister(
            AuthenticatedUserPrincipal principal,
            UnregisterSubscriptionRequest request
    ) {
        PushSubscription subscription = pushSubscriptionRepository.findByUserIdAndEndpointHash(principal.userId(), sha256(request.endpoint()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "푸시 구독을 찾을 수 없습니다."));
        subscription.setStatus("INACTIVE");
        return toSubscriptionResponse(pushSubscriptionRepository.save(subscription));
    }

    public List<Map<String, Object>> getSubscriptions(AuthenticatedUserPrincipal principal) {
        return pushSubscriptionRepository.findByUserIdOrderByIdDesc(principal.userId()).stream()
                .map(this::toSubscriptionResponse)
                .toList();
    }

    public Map<String, Object> getPreference(AuthenticatedUserPrincipal principal) {
        return toPreferenceResponse(getOrCreatePreference(principal.userId()));
    }

    @Transactional
    public Map<String, Object> updatePreference(
            AuthenticatedUserPrincipal principal,
            UpdatePreferenceRequest request
    ) {
        PushPreference preference = getOrCreatePreference(principal.userId());
        preference.setMarketingEnabled(request.marketingEnabled());
        preference.setOrderEnabled(request.orderEnabled());
        preference.setChatEnabled(request.chatEnabled());
        preference.setDealEnabled(request.dealEnabled());
        return toPreferenceResponse(pushPreferenceRepository.save(preference));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private PushPreference getOrCreatePreference(Long userId) {
        return pushPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    PushPreference preference = new PushPreference();
                    preference.setUser(getUser(userId));
                    return pushPreferenceRepository.save(preference);
                });
    }

    private Map<String, Object> toSubscriptionResponse(PushSubscription subscription) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", subscription.getId());
        response.put("endpoint", subscription.getEndpoint());
        response.put("status", subscription.getStatus());
        response.put("p256dh", subscription.getP256dh());
        response.put("auth", subscription.getAuth());
        response.put("vapidPublicKey", subscription.getVapidPublicKey());
        response.put("createdAt", subscription.getCreatedAt());
        return response;
    }

    private Map<String, Object> toPreferenceResponse(PushPreference preference) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("marketingEnabled", preference.isMarketingEnabled());
        response.put("orderEnabled", preference.isOrderEnabled());
        response.put("chatEnabled", preference.isChatEnabled());
        response.put("dealEnabled", preference.isDealEnabled());
        return response;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte current : bytes) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }
}
