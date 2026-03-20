package com.pbshop.java.spring.maven.jpa.postgresql.news;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.news.dto.NewsDtos.CreateNewsCategoryRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.news.dto.NewsDtos.CreateNewsRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.news.dto.NewsDtos.UpdateNewsRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional
public class NewsService {
    private final NewsRepository newsRepository;
    private final NewsCategoryRepository categoryRepository;
    private final NewsProductRepository newsProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public NewsService(NewsRepository newsRepository, NewsCategoryRepository categoryRepository,
            NewsProductRepository newsProductRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
        this.newsProductRepository = newsProductRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> list(Long categoryId) {
        List<Map<String, Object>> items = (categoryId == null ? newsRepository.findAllByOrderByIdDesc() : newsRepository.findByCategoryIdOrderByIdDesc(categoryId))
                .stream().map(this::toSummary).toList();
        return Map.of("items", items, "pagination", Map.of("page", 1, "limit", items.isEmpty() ? 20 : items.size(), "total", items.size(), "totalPages", items.isEmpty() ? 0 : 1));
    }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> categories() {
        return categoryRepository.findAllByOrderByIdAsc().stream().map(category -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", category.getId());
            item.put("name", category.getName());
            item.put("slug", category.getSlug());
            return item;
        }).toList();
    }
    @Transactional(readOnly = true)
    public Map<String, Object> show(Long id) {
        News news = requireNews(id);
        List<Map<String, Object>> products = newsProductRepository.findByNewsIdOrderByIdAsc(news.getId()).stream()
                .map(rel -> {
                    Map<String, Object> product = new LinkedHashMap<>();
                    product.put("id", rel.getProduct().getId());
                    product.put("name", rel.getProduct().getName());
                    product.put("slug", rel.getProduct().getSlug());
                    return product;
                }).toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", news.getId());
        response.put("categoryId", news.getCategory() == null ? null : news.getCategory().getId());
        response.put("authorId", news.getAuthor().getId());
        response.put("title", news.getTitle());
        response.put("content", news.getContent());
        response.put("thumbnailUrl", news.getThumbnailUrl() == null ? "" : news.getThumbnailUrl());
        response.put("products", products);
        response.put("createdAt", news.getCreatedAt() == null ? null : news.getCreatedAt().toString());
        response.put("updatedAt", news.getUpdatedAt() == null ? null : news.getUpdatedAt().toString());
        return response;
    }
    public Map<String, Object> create(AuthenticatedUserPrincipal principal, CreateNewsRequest request) {
        User actor = requireAdminUser(principal);
        News news = new News();
        news.setCategory(request.categoryId() == null ? null : categoryRepository.findById(request.categoryId()).orElse(null));
        news.setAuthor(actor);
        news.setTitle(request.title());
        news.setContent(request.content());
        news.setThumbnailUrl(request.thumbnailUrl());
        News saved = newsRepository.save(news);
        syncProducts(saved, request.productIds());
        return show(saved.getId());
    }
    public Map<String, Object> update(AuthenticatedUserPrincipal principal, Long id, UpdateNewsRequest request) {
        requireAdminUser(principal);
        News news = requireNews(id);
        if (request.categoryId() != null || request.categoryId() == null) { news.setCategory(request.categoryId() == null ? null : categoryRepository.findById(request.categoryId()).orElse(null)); }
        if (request.title() != null && !request.title().isBlank()) { news.setTitle(request.title()); }
        if (request.content() != null && !request.content().isBlank()) { news.setContent(request.content()); }
        if (request.thumbnailUrl() != null || request.thumbnailUrl() == null) { news.setThumbnailUrl(request.thumbnailUrl()); }
        if (request.productIds() != null) { syncProducts(news, request.productIds()); }
        return show(news.getId());
    }
    public Map<String, Object> delete(AuthenticatedUserPrincipal principal, Long id) { requireAdminUser(principal); newsRepository.delete(requireNews(id)); return Map.of("message", "뉴스가 삭제되었습니다."); }
    public Map<String, Object> createCategory(AuthenticatedUserPrincipal principal, CreateNewsCategoryRequest request) {
        requireAdminUser(principal);
        NewsCategory category = new NewsCategory();
        category.setName(request.name());
        category.setSlug((request.slug() == null || request.slug().isBlank() ? request.name() : request.slug()).toLowerCase().replace(" ", "-"));
        categoryRepository.save(category);
        return Map.of("id", category.getId(), "name", category.getName(), "slug", category.getSlug());
    }
    public Map<String, Object> deleteCategory(AuthenticatedUserPrincipal principal, Long id) { requireAdminUser(principal); categoryRepository.delete(categoryRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "뉴스 카테고리를 찾을 수 없습니다."))); return Map.of("message", "뉴스 카테고리가 삭제되었습니다."); }
    private void syncProducts(News news, List<Long> productIds) {
        newsProductRepository.deleteByNewsId(news.getId());
        if (productIds == null) { return; }
        for (Long productId : productIds) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                NewsProduct relation = new NewsProduct();
                relation.setNews(news);
                relation.setProduct(product);
                newsProductRepository.save(relation);
            }
        }
    }
    private News requireNews(Long id) { return newsRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "뉴스를 찾을 수 없습니다.")); }
    private User requireAdminUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) { throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."); }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) { throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다."); }
        return userRepository.findById(principal.userId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
    private Map<String, Object> toSummary(News news) {
        return Map.of("id", news.getId(), "categoryId", news.getCategory() == null ? null : news.getCategory().getId(), "authorId", news.getAuthor().getId(), "title", news.getTitle(), "content", news.getContent(), "thumbnailUrl", news.getThumbnailUrl() == null ? "" : news.getThumbnailUrl(), "createdAt", news.getCreatedAt() == null ? null : news.getCreatedAt().toString(), "updatedAt", news.getUpdatedAt() == null ? null : news.getUpdatedAt().toString());
    }
}
