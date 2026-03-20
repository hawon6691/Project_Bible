package com.pbshop.java.spring.maven.jpa.postgresql.category;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.category.dto.CategoryDtos.SaveCategoryRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCategoryTree() {
        List<Category> categories = categoryRepository.findAllByOrderByDepthAscSortOrderAscIdAsc();
        Map<Long, Map<String, Object>> nodes = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (Category category : categories) {
            Map<String, Object> current = toCategoryNode(category);
            nodes.put(category.getId(), current);

            Long parentId = category.getParent() == null ? null : category.getParent().getId();
            if (parentId == null) {
                roots.add(current);
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) nodes.get(parentId).get("children");
            children.add(current);
        }

        return roots;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCategory(Long id) {
        Category category = findCategory(id);
        Map<String, Object> response = new LinkedHashMap<>(toCategoryFlat(category));
        response.put("parent", category.getParent() == null ? null : toCategoryParent(category.getParent()));
        return response;
    }

    public Map<String, Object> createCategory(AuthenticatedUserPrincipal principal, SaveCategoryRequest request) {
        requireAdmin(principal);

        Category category = new Category();
        applyCategoryRequest(category, request, null);
        return toCategoryFlat(categoryRepository.save(category));
    }

    public Map<String, Object> updateCategory(AuthenticatedUserPrincipal principal, Long id, SaveCategoryRequest request) {
        requireAdmin(principal);

        Category category = findCategory(id);
        applyCategoryRequest(category, request, id);
        return toCategoryFlat(categoryRepository.save(category));
    }

    public Map<String, Object> deleteCategory(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);

        Category category = findCategory(id);
        if (categoryRepository.existsByParentId(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "하위 카테고리가 존재하여 삭제할 수 없습니다.");
        }

        categoryRepository.delete(category);
        return Map.of("message", "카테고리가 삭제되었습니다.");
    }

    private void applyCategoryRequest(Category category, SaveCategoryRequest request, Long currentId) {
        Category parent = request.parentId() == null ? null : findCategory(request.parentId());
        if (currentId != null && parent != null && currentId.equals(parent.getId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "카테고리 자신을 부모로 지정할 수 없습니다.");
        }

        String slug = (request.slug() == null || request.slug().isBlank())
                ? slugify(request.name())
                : slugify(request.slug());

        boolean duplicate = currentId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, currentId);
        if (duplicate) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 사용 중인 slug 입니다.");
        }

        category.setParent(parent);
        category.setName(request.name());
        category.setSlug(slug);
        category.setDepth(parent == null ? 0 : parent.getDepth() + 1);
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setVisible(request.isVisible() == null || request.isVisible());
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
    }

    private Map<String, Object> toCategoryNode(Category category) {
        Map<String, Object> response = new LinkedHashMap<>(toCategoryFlat(category));
        response.put("children", new ArrayList<Map<String, Object>>());
        return response;
    }

    private Map<String, Object> toCategoryFlat(Category category) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", category.getId());
        response.put("parentId", category.getParent() == null ? null : category.getParent().getId());
        response.put("name", category.getName());
        response.put("slug", category.getSlug());
        response.put("depth", category.getDepth());
        response.put("sortOrder", category.getSortOrder());
        response.put("isVisible", category.isVisible());
        return response;
    }

    private Map<String, Object> toCategoryParent(Category category) {
        return Map.of(
                "id", category.getId(),
                "name", category.getName(),
                "slug", category.getSlug()
        );
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        return normalized.isBlank() ? "category" : normalized;
    }
}
