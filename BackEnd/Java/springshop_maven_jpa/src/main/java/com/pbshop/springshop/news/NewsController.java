package com.pbshop.springshop.news;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.news.dto.NewsDtos.CreateNewsCategoryRequest;
import com.pbshop.springshop.news.dto.NewsDtos.CreateNewsRequest;
import com.pbshop.springshop.news.dto.NewsDtos.UpdateNewsRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/news")
public class NewsController {
    private final NewsService newsService;
    public NewsController(NewsService newsService) { this.newsService = newsService; }
    @GetMapping public ApiResponse<Map<String, Object>> list(@RequestParam(required = false) Long categoryId) { return ApiResponse.success(newsService.list(categoryId)); }
    @GetMapping("/categories") public ApiResponse<List<Map<String, Object>>> categories() { return ApiResponse.success(newsService.categories()); }
    @GetMapping("/{id}") public ApiResponse<Map<String, Object>> show(@PathVariable Long id) { return ApiResponse.success(newsService.show(id)); }
    @PostMapping("/admin") public ApiResponse<Map<String, Object>> create(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @Valid @RequestBody CreateNewsRequest request) { return ApiResponse.success(newsService.create(principal, request)); }
    @PostMapping("/admin/{id}") public ApiResponse<Map<String, Object>> update(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id, @Valid @RequestBody UpdateNewsRequest request) { return ApiResponse.success(newsService.update(principal, id, request)); }
    @DeleteMapping("/admin/{id}") public ApiResponse<Map<String, Object>> delete(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(newsService.delete(principal, id)); }
    @PostMapping("/admin/categories") public ApiResponse<Map<String, Object>> createCategory(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @Valid @RequestBody CreateNewsCategoryRequest request) { return ApiResponse.success(newsService.createCategory(principal, request)); }
    @DeleteMapping("/admin/categories/{id}") public ApiResponse<Map<String, Object>> deleteCategory(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(newsService.deleteCategory(principal, id)); }
}
