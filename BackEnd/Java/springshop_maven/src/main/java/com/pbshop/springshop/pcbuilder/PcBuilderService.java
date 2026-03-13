package com.pbshop.springshop.pcbuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.pcbuilder.dto.PcBuilderDtos.AddPartRequest;
import com.pbshop.springshop.pcbuilder.dto.PcBuilderDtos.CompatibilityRuleRequest;
import com.pbshop.springshop.pcbuilder.dto.PcBuilderDtos.CreateBuildRequest;
import com.pbshop.springshop.pcbuilder.dto.PcBuilderDtos.UpdateBuildRequest;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class PcBuilderService {
    private final PcBuildRepository pcBuildRepository;
    private final PcBuildPartRepository pcBuildPartRepository;
    private final PcCompatibilityRuleRepository ruleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public PcBuilderService(PcBuildRepository pcBuildRepository, PcBuildPartRepository pcBuildPartRepository,
            PcCompatibilityRuleRepository ruleRepository, ProductRepository productRepository, UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.pcBuildRepository = pcBuildRepository;
        this.pcBuildPartRepository = pcBuildPartRepository;
        this.ruleRepository = ruleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listMine(AuthenticatedUserPrincipal principal) {
        User user = requireCurrentUser(principal);
        List<Map<String, Object>> items = pcBuildRepository.findByUserIdOrderByIdDesc(user.getId()).stream().map(this::toSummary).toList();
        return page(items);
    }
    public Map<String, Object> create(AuthenticatedUserPrincipal principal, CreateBuildRequest request) {
        User user = requireCurrentUser(principal);
        PcBuild build = new PcBuild();
        build.setUser(user);
        build.setName(request.name());
        build.setDescription(request.description());
        return toDetail(pcBuildRepository.save(build));
    }
    @Transactional(readOnly = true)
    public Map<String, Object> show(Long id) {
        PcBuild build = requireBuild(id);
        build.setViewCount(build.getViewCount() + 1);
        return toDetail(build);
    }
    public Map<String, Object> update(AuthenticatedUserPrincipal principal, Long id, UpdateBuildRequest request) {
        PcBuild build = requireOwnedBuild(principal, id);
        if (request.name() != null && !request.name().isBlank()) { build.setName(request.name()); }
        if (request.description() != null || request.description() == null) { build.setDescription(request.description()); }
        return toDetail(build);
    }
    public Map<String, Object> delete(AuthenticatedUserPrincipal principal, Long id) {
        pcBuildRepository.delete(requireOwnedBuild(principal, id));
        return Map.of("message", "견적이 삭제되었습니다.");
    }
    public Map<String, Object> addPart(AuthenticatedUserPrincipal principal, Long buildId, AddPartRequest request) {
        PcBuild build = requireOwnedBuild(principal, buildId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        PcBuildPart part = new PcBuildPart();
        part.setPcBuild(build);
        part.setPartType(request.partType());
        part.setProduct(product);
        part.setQuantity(request.quantity() == null ? 1 : request.quantity());
        pcBuildPartRepository.save(part);
        return toDetail(build);
    }
    public Map<String, Object> removePart(AuthenticatedUserPrincipal principal, Long buildId, Long partId) {
        PcBuild build = requireOwnedBuild(principal, buildId);
        PcBuildPart part = pcBuildPartRepository.findByIdAndPcBuildId(partId, build.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "부품을 찾을 수 없습니다."));
        pcBuildPartRepository.delete(part);
        return toDetail(build);
    }
    @Transactional(readOnly = true)
    public Map<String, Object> compatibility(Long buildId) {
        PcBuild build = requireBuild(buildId);
        List<PcBuildPart> parts = pcBuildPartRepository.findByPcBuildIdOrderByIdAsc(build.getId());
        List<Map<String, Object>> issues = ruleRepository.findAllByOrderByIdAsc().stream()
                .filter(rule -> parts.stream().anyMatch(part -> part.getPartType().equalsIgnoreCase(rule.getSourcePartType()))
                        && parts.stream().anyMatch(part -> part.getPartType().equalsIgnoreCase(rule.getTargetPartType())))
                .filter(rule -> "CATEGORY_MISMATCH".equalsIgnoreCase(rule.getRuleType()))
                .map(rule -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("ruleId", rule.getId());
                    item.put("message", rule.getName());
                    item.put("severity", "WARNING");
                    return item;
                })
                .toList();
        return Map.of("compatible", issues.isEmpty(), "issues", issues);
    }
    public Map<String, Object> share(AuthenticatedUserPrincipal principal, Long buildId) {
        PcBuild build = requireOwnedBuild(principal, buildId);
        if (build.getShareCode() == null || build.getShareCode().isBlank()) {
            build.setShareCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        }
        return Map.of("shareUrl", "/api/v1/pc-builds/shared/" + build.getShareCode(), "shareCode", build.getShareCode());
    }
    @Transactional(readOnly = true)
    public Map<String, Object> shared(String shareCode) { return toDetail(pcBuildRepository.findByShareCode(shareCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공유 견적을 찾을 수 없습니다."))); }
    @Transactional(readOnly = true)
    public Map<String, Object> popular() { return page(pcBuildRepository.findAllByOrderByViewCountDescIdDesc().stream().map(this::toSummary).toList()); }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> rules(AuthenticatedUserPrincipal principal) { requireAdmin(principal); return ruleRepository.findAllByOrderByIdAsc().stream().map(this::toRule).toList(); }
    public Map<String, Object> createRule(AuthenticatedUserPrincipal principal, CompatibilityRuleRequest request) {
        requireAdmin(principal);
        PcCompatibilityRule rule = new PcCompatibilityRule();
        rule.setName(request.name());
        rule.setSourcePartType(request.sourcePartType());
        rule.setTargetPartType(request.targetPartType());
        rule.setRuleType(request.ruleType());
        rule.setRuleValueJson(writeJson(request.ruleValue()));
        return toRule(ruleRepository.save(rule));
    }
    public Map<String, Object> updateRule(AuthenticatedUserPrincipal principal, Long id, CompatibilityRuleRequest request) {
        requireAdmin(principal);
        PcCompatibilityRule rule = ruleRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "호환성 규칙을 찾을 수 없습니다."));
        rule.setName(request.name());
        rule.setSourcePartType(request.sourcePartType());
        rule.setTargetPartType(request.targetPartType());
        rule.setRuleType(request.ruleType());
        rule.setRuleValueJson(writeJson(request.ruleValue()));
        return toRule(rule);
    }
    public Map<String, Object> deleteRule(AuthenticatedUserPrincipal principal, Long id) { requireAdmin(principal); ruleRepository.delete(ruleRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "호환성 규칙을 찾을 수 없습니다."))); return Map.of("message", "호환성 규칙이 삭제되었습니다."); }

    private PcBuild requireBuild(Long id) { return pcBuildRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "견적을 찾을 수 없습니다.")); }
    private PcBuild requireOwnedBuild(AuthenticatedUserPrincipal principal, Long id) {
        User user = requireCurrentUser(principal);
        return pcBuildRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "견적을 찾을 수 없습니다."));
    }
    private User requireCurrentUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) { throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."); }
        return userRepository.findById(principal.userId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) { throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."); }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) { throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다."); }
    }
    private Map<String, Object> page(List<Map<String, Object>> items) { return Map.of("items", items, "pagination", Map.of("page", 1, "limit", items.isEmpty() ? 20 : items.size(), "total", items.size(), "totalPages", items.isEmpty() ? 0 : 1)); }
    private Map<String, Object> toSummary(PcBuild build) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", build.getId());
        item.put("name", build.getName());
        item.put("description", build.getDescription() == null ? "" : build.getDescription());
        item.put("shareCode", build.getShareCode() == null ? "" : build.getShareCode());
        item.put("viewCount", build.getViewCount());
        item.put("createdAt", build.getCreatedAt() == null ? null : build.getCreatedAt().toString());
        item.put("updatedAt", build.getUpdatedAt() == null ? null : build.getUpdatedAt().toString());
        return item;
    }
    private Map<String, Object> toDetail(PcBuild build) {
        List<Map<String, Object>> parts = pcBuildPartRepository.findByPcBuildIdOrderByIdAsc(build.getId()).stream().map(part -> {
            Map<String, Object> product = new LinkedHashMap<>();
            product.put("id", part.getProduct().getId());
            product.put("name", part.getProduct().getName());
            product.put("slug", part.getProduct().getSlug());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", part.getId());
            item.put("partType", part.getPartType());
            item.put("quantity", part.getQuantity());
            item.put("product", product);
            return item;
        }).toList();
        Map<String, Object> response = new LinkedHashMap<>(toSummary(build));
        response.put("userId", build.getUser().getId());
        response.put("parts", parts);
        return response;
    }
    private Map<String, Object> toRule(PcCompatibilityRule rule) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", rule.getId());
        item.put("name", rule.getName());
        item.put("sourcePartType", rule.getSourcePartType());
        item.put("targetPartType", rule.getTargetPartType());
        item.put("ruleType", rule.getRuleType());
        item.put("ruleValue", readJson(rule.getRuleValueJson()));
        return item;
    }
    private String writeJson(Map<String, Object> map) { try { return map == null ? null : objectMapper.writeValueAsString(map); } catch (JsonProcessingException e) { throw new BusinessException(ErrorCode.INTERNAL_ERROR, "호환성 규칙 저장에 실패했습니다."); } }
    private Map<String, Object> readJson(String value) { try { return value == null || value.isBlank() ? Map.of() : objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {}); } catch (JsonProcessingException e) { throw new BusinessException(ErrorCode.INTERNAL_ERROR, "호환성 규칙을 읽을 수 없습니다."); } }
}
