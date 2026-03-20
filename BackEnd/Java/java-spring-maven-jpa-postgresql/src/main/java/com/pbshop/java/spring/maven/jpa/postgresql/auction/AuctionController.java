package com.pbshop.java.spring.maven.jpa.postgresql.auction;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auction.dto.AuctionDtos.CreateAuctionBidRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.auction.dto.AuctionDtos.CreateAuctionRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.auction.dto.AuctionDtos.UpdateAuctionBidRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        return ApiResponse.success(auctionService.list(status, categoryId, page, limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> show(@PathVariable Long id) {
        return ApiResponse.success(auctionService.detail(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreateAuctionRequest request
    ) {
        return ApiResponse.success(auctionService.create(principal, request));
    }

    @PostMapping("/{id}/bids")
    public ApiResponse<Map<String, Object>> createBid(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CreateAuctionBidRequest request
    ) {
        return ApiResponse.success(auctionService.createBid(principal, id, request));
    }

    @PatchMapping("/{id}/bids/{bidId}/select")
    public ApiResponse<Map<String, Object>> selectBid(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long bidId
    ) {
        return ApiResponse.success(auctionService.selectBid(principal, id, bidId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(auctionService.cancel(principal, id));
    }

    @PatchMapping("/{id}/bids/{bidId}")
    public ApiResponse<Map<String, Object>> updateBid(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long bidId,
            @Valid @RequestBody UpdateAuctionBidRequest request
    ) {
        return ApiResponse.success(auctionService.updateBid(principal, id, bidId, request));
    }

    @DeleteMapping("/{id}/bids/{bidId}")
    public ApiResponse<Map<String, Object>> deleteBid(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long bidId
    ) {
        return ApiResponse.success(auctionService.deleteBid(principal, id, bidId));
    }
}
