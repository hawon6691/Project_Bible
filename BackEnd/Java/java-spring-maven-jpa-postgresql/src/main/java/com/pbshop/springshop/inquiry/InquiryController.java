package com.pbshop.springshop.inquiry;

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
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.inquiry.dto.InquiryDtos.AnswerInquiryRequest;
import com.pbshop.springshop.inquiry.dto.InquiryDtos.CreateInquiryRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping("/products/{productId}/inquiries")
    public ApiResponse<List<Map<String, Object>>> getProductInquiries(@PathVariable Long productId) {
        return ApiResponse.success(inquiryService.getProductInquiries(productId));
    }

    @PostMapping("/products/{productId}/inquiries")
    public ApiResponse<Map<String, Object>> createInquiry(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody CreateInquiryRequest request
    ) {
        return ApiResponse.success(inquiryService.createInquiry(principal, productId, request));
    }

    @PostMapping("/inquiries/{id}/answer")
    public ApiResponse<Map<String, Object>> answerInquiry(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AnswerInquiryRequest request
    ) {
        return ApiResponse.success(inquiryService.answerInquiry(principal, id, request));
    }

    @GetMapping("/inquiries/me")
    public ApiResponse<List<Map<String, Object>>> getMyInquiries(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(inquiryService.getMyInquiries(principal));
    }

    @DeleteMapping("/inquiries/{id}")
    public ApiResponse<Map<String, Object>> deleteInquiry(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(inquiryService.deleteInquiry(principal, id));
    }
}
