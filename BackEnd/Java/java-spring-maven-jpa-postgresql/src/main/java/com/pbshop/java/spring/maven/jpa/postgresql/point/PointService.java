package com.pbshop.java.spring.maven.jpa.postgresql.point;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.point.dto.PointDtos.GrantPointRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class PointService {

    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;

    public PointService(PointTransactionRepository pointTransactionRepository, UserRepository userRepository) {
        this.pointTransactionRepository = pointTransactionRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getBalance(AuthenticatedUserPrincipal principal) {
        User user = getUser(principal.userId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("balance", user.getPointBalance());
        response.put("expiringSoon", BigDecimal.ZERO);
        response.put("expiringDate", null);
        return response;
    }

    public Map<String, Object> getTransactions(AuthenticatedUserPrincipal principal, String type, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<PointTransaction> transactionPage = (type == null || type.isBlank())
                ? pointTransactionRepository.findByUserIdOrderByIdDesc(principal.userId(), pageable)
                : pointTransactionRepository.findByUserIdAndTypeOrderByIdDesc(principal.userId(), type, pageable);

        return Map.of(
                "items", transactionPage.getContent().stream().map(this::toResponse).toList(),
                "page", transactionPage.getNumber() + 1,
                "limit", transactionPage.getSize(),
                "total", transactionPage.getTotalElements(),
                "totalPages", transactionPage.getTotalPages()
        );
    }

    @Transactional
    public Map<String, Object> grant(AuthenticatedUserPrincipal principal, GrantPointRequest request) {
        requireAdmin(principal);
        User user = getUser(request.userId());
        PointTransaction transaction = addPoints(
                user,
                "ADMIN_GRANT",
                request.amount(),
                request.description(),
                "ADMIN_GRANT",
                user.getId()
        );
        return toResponse(transaction);
    }

    @Transactional
    public PointTransaction addPoints(User user, String type, BigDecimal amount, String description, String referenceType, Long referenceId) {
        BigDecimal nextBalance = user.getPointBalance().add(amount);
        user.setPointBalance(nextBalance);
        userRepository.save(user);

        PointTransaction transaction = new PointTransaction();
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceSnapshot(nextBalance);
        transaction.setDescription(description);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        return pointTransactionRepository.save(transaction);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private Map<String, Object> toResponse(PointTransaction transaction) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", transaction.getId());
        response.put("userId", transaction.getUser().getId());
        response.put("type", transaction.getType());
        response.put("amount", transaction.getAmount());
        response.put("balanceSnapshot", transaction.getBalanceSnapshot());
        response.put("description", transaction.getDescription());
        response.put("referenceType", transaction.getReferenceType());
        response.put("referenceId", transaction.getReferenceId());
        response.put("createdAt", transaction.getCreatedAt());
        return response;
    }
}
