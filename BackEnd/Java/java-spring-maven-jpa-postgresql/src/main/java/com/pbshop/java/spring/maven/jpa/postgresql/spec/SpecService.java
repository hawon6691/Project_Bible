package com.pbshop.java.spring.maven.jpa.postgresql.spec;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.category.Category;
import com.pbshop.java.spring.maven.jpa.postgresql.category.CategoryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductSpec;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductSpecRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.spec.dto.SpecDtos.ProductSpecValueRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.spec.dto.SpecDtos.SaveSpecDefinitionRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.spec.dto.SpecDtos.SetProductSpecsRequest;

@Service
@Transactional
public class SpecService {

    private final SpecDefinitionRepository specDefinitionRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSpecRepository productSpecRepository;
    private final ObjectMapper objectMapper;

    public SpecService(
            SpecDefinitionRepository specDefinitionRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductSpecRepository productSpecRepository,
            ObjectMapper objectMapper
    ) {
        this.specDefinitionRepository = specDefinitionRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productSpecRepository = productSpecRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDefinitions(Long categoryId) {
        List<SpecDefinition> definitions = categoryId == null
                ? specDefinitionRepository.findAllByOrderBySortOrderAscIdAsc()
                : specDefinitionRepository.findByCategoryIdOrderBySortOrderAscIdAsc(categoryId);

        return definitions.stream().map(this::toDefinitionResponse).toList();
    }

    public Map<String, Object> createDefinition(
            AuthenticatedUserPrincipal principal,
            SaveSpecDefinitionRequest request
    ) {
        requireAdmin(principal);
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        String specKey = slugify(request.key());
        if (specDefinitionRepository.existsByCategoryIdAndSpecKey(category.getId(), specKey)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 등록된 스펙 키입니다.");
        }

        SpecDefinition definition = new SpecDefinition();
        applyDefinition(definition, category, request);
        definition.setSpecKey(specKey);

        return toDefinitionResponse(specDefinitionRepository.save(definition));
    }

    public Map<String, Object> updateDefinition(
            AuthenticatedUserPrincipal principal,
            Long id,
            SaveSpecDefinitionRequest request
    ) {
        requireAdmin(principal);
        SpecDefinition definition = specDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "스펙 정의를 찾을 수 없습니다."));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        String specKey = slugify(request.key());
        if (specDefinitionRepository.existsByCategoryIdAndSpecKeyAndIdNot(category.getId(), specKey, id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 등록된 스펙 키입니다.");
        }

        applyDefinition(definition, category, request);
        definition.setSpecKey(specKey);

        return toDefinitionResponse(specDefinitionRepository.save(definition));
    }

    public Map<String, Object> deleteDefinition(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        SpecDefinition definition = specDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "스펙 정의를 찾을 수 없습니다."));
        specDefinitionRepository.delete(definition);
        return Map.of("message", "스펙 정의가 삭제되었습니다.");
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductSpecs(Long productId) {
        ensureProduct(productId);
        return productSpecRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .map(this::toProductSpecResponse)
                .toList();
    }

    public List<Map<String, Object>> setProductSpecs(
            AuthenticatedUserPrincipal principal,
            Long productId,
            SetProductSpecsRequest request
    ) {
        requireAdmin(principal);
        Product product = ensureProduct(productId);
        productSpecRepository.deleteAll(productSpecRepository.findByProductIdOrderBySortOrderAsc(productId));
        product.getSpecs().clear();

        int fallbackOrder = 0;
        for (ProductSpecValueRequest item : request.specs()) {
            ProductSpec spec = new ProductSpec();
            spec.setProduct(product);
            spec.setSpecKey(item.key());
            spec.setSpecValue(item.value());
            spec.setSortOrder(item.sortOrder() == null ? fallbackOrder : item.sortOrder());
            fallbackOrder++;
            product.getSpecs().add(spec);
            productSpecRepository.save(spec);
        }

        return productSpecRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .map(this::toProductSpecResponse)
                .toList();
    }

    private void applyDefinition(
            SpecDefinition definition,
            Category category,
            SaveSpecDefinitionRequest request
    ) {
        definition.setCategory(category);
        definition.setName(request.name());
        definition.setInputType(request.inputType() == null || request.inputType().isBlank() ? "TEXT" : request.inputType());
        definition.setOptionsJson(writeOptionsJson(request.options()));
        definition.setUnit(request.unit());
        definition.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        definition.setFilterable(request.filterable() == null || request.filterable());
    }

    private Map<String, Object> toDefinitionResponse(SpecDefinition definition) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", definition.getId());
        response.put("categoryId", definition.getCategory().getId());
        response.put("categoryName", definition.getCategory().getName());
        response.put("key", definition.getSpecKey());
        response.put("name", definition.getName());
        response.put("inputType", definition.getInputType());
        response.put("options", readOptionsJson(definition.getOptionsJson()));
        response.put("unit", definition.getUnit() == null ? "" : definition.getUnit());
        response.put("sortOrder", definition.getSortOrder());
        response.put("filterable", definition.isFilterable());
        return response;
    }

    private Map<String, Object> toProductSpecResponse(ProductSpec spec) {
        return Map.of(
                "id", spec.getId(),
                "key", spec.getSpecKey(),
                "value", spec.getSpecValue(),
                "sortOrder", spec.getSortOrder()
        );
    }

    private Product ensureProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private String writeOptionsJson(List<String> options) {
        try {
            return options == null || options.isEmpty() ? null : objectMapper.writeValueAsString(options);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "스펙 옵션 직렬화에 실패했습니다.");
        }
    }

    private List<String> readOptionsJson(String optionsJson) {
        try {
            return optionsJson == null || optionsJson.isBlank()
                    ? List.of()
                    : objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {
                    });
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "스펙 옵션 역직렬화에 실패했습니다.");
        }
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        return normalized.isBlank() ? "spec" : normalized;
    }
}
