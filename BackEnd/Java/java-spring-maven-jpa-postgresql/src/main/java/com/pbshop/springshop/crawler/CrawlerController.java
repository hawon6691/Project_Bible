package com.pbshop.springshop.crawler;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.crawler.dto.CrawlerDtos.CreateCrawlerJobRequest;
import com.pbshop.springshop.crawler.dto.CrawlerDtos.TriggerCrawlerRequest;
import com.pbshop.springshop.crawler.dto.CrawlerDtos.UpdateCrawlerJobRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/crawler/admin")
public class CrawlerController {

    private final CrawlerService crawlerService;

    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @GetMapping("/jobs")
    public ApiResponse<Map<String, Object>> jobs(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(crawlerService.jobs(principal, status, page, limit));
    }

    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Object>> createJob(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreateCrawlerJobRequest request
    ) {
        return ApiResponse.success(crawlerService.createJob(principal, request));
    }

    @PatchMapping("/jobs/{id}")
    public ApiResponse<Map<String, Object>> updateJob(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @RequestBody UpdateCrawlerJobRequest request
    ) {
        return ApiResponse.success(crawlerService.updateJob(principal, id, request));
    }

    @DeleteMapping("/jobs/{id}")
    public ApiResponse<Map<String, Object>> deleteJob(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(crawlerService.deleteJob(principal, id));
    }

    @PostMapping("/jobs/{id}/run")
    public ApiResponse<Map<String, Object>> runJob(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(crawlerService.runJob(principal, id));
    }

    @PostMapping("/triggers")
    public ApiResponse<Map<String, Object>> trigger(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestBody TriggerCrawlerRequest request
    ) {
        return ApiResponse.success(crawlerService.trigger(principal, request));
    }

    @GetMapping("/runs")
    public ApiResponse<Map<String, Object>> runs(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(crawlerService.runs(principal, status, jobId, page, limit));
    }

    @GetMapping("/monitoring")
    public ApiResponse<Map<String, Object>> monitoring(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(crawlerService.monitoring(principal));
    }
}
