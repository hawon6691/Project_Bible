package com.pbshop.springshop.auction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbshop.springshop.auction.dto.AuctionDtos.CreateAuctionBidRequest;
import com.pbshop.springshop.auction.dto.AuctionDtos.CreateAuctionRequest;
import com.pbshop.springshop.auction.dto.AuctionDtos.UpdateAuctionBidRequest;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.category.CategoryRepository;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    public AuctionService(
            AuctionRepository auctionRepository,
            AuctionBidRepository auctionBidRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ObjectMapper objectMapper
    ) {
        this.auctionRepository = auctionRepository;
        this.auctionBidRepository = auctionBidRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> list(String status, Long categoryId, int page, int limit) {
        List<Auction> items = findAuctions(status, categoryId);
        return Map.of(
                "items", items.stream().map(this::toSummary).toList(),
                "pagination", Map.of("page", page, "limit", limit, "total", items.size(), "totalPages", items.isEmpty() ? 0 : 1)
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(Long id) {
        Auction auction = requireAuction(id);
        List<Map<String, Object>> bids = auctionBidRepository.findByAuctionIdOrderByIdAsc(id).stream()
                .map(this::toBid)
                .toList();
        Map<String, Object> response = new LinkedHashMap<>(toSummary(auction));
        response.put("description", auction.getDescription());
        response.put("specs", readSpecs(auction.getSpecsJson()));
        response.put("bids", bids);
        return response;
    }

    public Map<String, Object> create(AuthenticatedUserPrincipal principal, CreateAuctionRequest request) {
        User actor = requireUser(principal);
        Auction auction = new Auction();
        auction.setUser(actor);
        auction.setCategory(request.categoryId() == null ? null : categoryRepository.findById(request.categoryId()).orElse(null));
        auction.setTitle(request.title());
        auction.setDescription(request.description());
        auction.setSpecsJson(writeSpecs(request.specs()));
        auction.setBudget(request.budget());
        auction.setStatus("OPEN");
        auctionRepository.save(auction);
        return detail(auction.getId());
    }

    public Map<String, Object> createBid(AuthenticatedUserPrincipal principal, Long auctionId, CreateAuctionBidRequest request) {
        User actor = requireUser(principal);
        Auction auction = requireAuction(auctionId);
        AuctionBid bid = new AuctionBid();
        bid.setAuction(auction);
        bid.setUser(actor);
        bid.setPrice(request.price());
        bid.setDescription(request.description());
        bid.setDeliveryDays(request.deliveryDays());
        bid.setStatus("ACTIVE");
        auctionBidRepository.save(bid);
        return toBid(bid);
    }

    public Map<String, Object> selectBid(AuthenticatedUserPrincipal principal, Long auctionId, Long bidId) {
        User actor = requireUser(principal);
        Auction auction = requireAuction(auctionId);
        if (!auction.getUser().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "역경매 소유자만 낙찰을 선택할 수 있습니다.");
        }
        AuctionBid bid = requireBid(auctionId, bidId);
        auction.setSelectedBid(bid);
        auction.setStatus("CLOSED");
        bid.setStatus("SELECTED");
        return Map.of("message", "낙찰을 선택했습니다.");
    }

    public Map<String, Object> cancel(AuthenticatedUserPrincipal principal, Long auctionId) {
        User actor = requireUser(principal);
        Auction auction = requireAuction(auctionId);
        if (!auction.getUser().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "역경매 소유자만 취소할 수 있습니다.");
        }
        auction.setStatus("CANCELLED");
        return Map.of("message", "역경매가 취소되었습니다.");
    }

    public Map<String, Object> updateBid(AuthenticatedUserPrincipal principal, Long auctionId, Long bidId, UpdateAuctionBidRequest request) {
        User actor = requireUser(principal);
        AuctionBid bid = requireBid(auctionId, bidId);
        if (!bid.getUser().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 입찰만 수정할 수 있습니다.");
        }
        if (request.price() != null) {
            bid.setPrice(request.price());
        }
        if (request.description() != null) {
            bid.setDescription(request.description());
        }
        if (request.deliveryDays() != null) {
            bid.setDeliveryDays(request.deliveryDays());
        }
        return toBid(bid);
    }

    public Map<String, Object> deleteBid(AuthenticatedUserPrincipal principal, Long auctionId, Long bidId) {
        User actor = requireUser(principal);
        AuctionBid bid = requireBid(auctionId, bidId);
        if (!bid.getUser().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 입찰만 삭제할 수 있습니다.");
        }
        auctionBidRepository.delete(bid);
        return Map.of("message", "입찰이 삭제되었습니다.");
    }

    private List<Auction> findAuctions(String status, Long categoryId) {
        if (status != null && categoryId != null) {
            return auctionRepository.findByStatusAndCategoryIdOrderByIdDesc(status, categoryId);
        }
        if (status != null) {
            return auctionRepository.findByStatusOrderByIdDesc(status);
        }
        if (categoryId != null) {
            return auctionRepository.findByCategoryIdOrderByIdDesc(categoryId);
        }
        return auctionRepository.findAllByOrderByIdDesc();
    }

    private Auction requireAuction(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "역경매를 찾을 수 없습니다."));
    }

    private AuctionBid requireBid(Long auctionId, Long bidId) {
        return auctionBidRepository.findByIdAndAuctionId(bidId, auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "입찰을 찾을 수 없습니다."));
    }

    private User requireUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private Map<String, Object> toSummary(Auction auction) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", auction.getId());
        response.put("userId", auction.getUser().getId());
        response.put("categoryId", auction.getCategory() == null ? null : auction.getCategory().getId());
        response.put("title", auction.getTitle());
        response.put("budget", auction.getBudget() == null ? null : auction.getBudget().setScale(2, RoundingMode.HALF_UP));
        response.put("status", auction.getStatus());
        response.put("selectedBidId", auction.getSelectedBid() == null ? null : auction.getSelectedBid().getId());
        response.put("createdAt", auction.getCreatedAt() == null ? null : auction.getCreatedAt().toString());
        response.put("updatedAt", auction.getUpdatedAt() == null ? null : auction.getUpdatedAt().toString());
        return response;
    }

    private Map<String, Object> toBid(AuctionBid bid) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", bid.getId());
        response.put("auctionId", bid.getAuction().getId());
        response.put("userId", bid.getUser().getId());
        response.put("price", bid.getPrice().setScale(2, RoundingMode.HALF_UP));
        response.put("description", bid.getDescription() == null ? "" : bid.getDescription());
        response.put("deliveryDays", bid.getDeliveryDays());
        response.put("status", bid.getStatus());
        response.put("createdAt", bid.getCreatedAt() == null ? null : bid.getCreatedAt().toString());
        return response;
    }

    private String writeSpecs(Map<String, Object> specs) {
        if (specs == null || specs.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(specs);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "역경매 사양 저장에 실패했습니다.");
        }
    }

    private Map<String, Object> readSpecs(String specsJson) {
        if (specsJson == null || specsJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(specsJson, new TypeReference<Map<String, Object>>() { });
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "역경매 사양 조회에 실패했습니다.");
        }
    }
}
