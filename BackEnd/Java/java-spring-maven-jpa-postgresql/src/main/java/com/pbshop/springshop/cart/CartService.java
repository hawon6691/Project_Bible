package com.pbshop.springshop.cart;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.cart.dto.CartDtos.StoreCartItemRequest;
import com.pbshop.springshop.cart.dto.CartDtos.UpdateCartItemRequest;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.product.SellerRepository;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            SellerRepository sellerRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCart(AuthenticatedUserPrincipal principal) {
        User user = requireUser(principal);
        List<CartItem> items = cartItemRepository.findByUserIdOrderByIdDesc(user.getId());
        BigDecimal totalAmount = items.stream()
                .map(item -> resolvePrice(item).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "items", items.stream().map(this::toResponse).toList(),
                "summary", Map.of(
                        "itemCount", items.size(),
                        "totalQuantity", items.stream().mapToInt(CartItem::getQuantity).sum(),
                        "totalAmount", totalAmount
                )
        );
    }

    public Map<String, Object> addToCart(AuthenticatedUserPrincipal principal, StoreCartItemRequest request) {
        User user = requireUser(principal);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        Seller seller = sellerRepository.findById(request.sellerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "판매처를 찾을 수 없습니다."));

        String optionsJson = writeOptions(request.selectedOptions());
        CartItem cartItem = cartItemRepository.findByUserIdAndProductIdAndSellerIdAndSelectedOptionsJson(
                        user.getId(),
                        product.getId(),
                        seller.getId(),
                        optionsJson
                )
                .orElseGet(CartItem::new);

        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setSeller(seller);
        cartItem.setSelectedOptionsJson(optionsJson);
        cartItem.setQuantity(cartItem.getId() == null ? request.quantity() : cartItem.getQuantity() + request.quantity());

        return toResponse(cartItemRepository.save(cartItem));
    }

    public Map<String, Object> updateCartItem(
            AuthenticatedUserPrincipal principal,
            Long itemId,
            UpdateCartItemRequest request
    ) {
        User user = requireUser(principal);
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다."));
        item.setQuantity(request.quantity());
        return toResponse(cartItemRepository.save(item));
    }

    public Map<String, Object> deleteCartItem(AuthenticatedUserPrincipal principal, Long itemId) {
        User user = requireUser(principal);
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다."));
        cartItemRepository.delete(item);
        return Map.of("message", "장바구니 항목이 삭제되었습니다.");
    }

    public Map<String, Object> clearCart(AuthenticatedUserPrincipal principal) {
        User user = requireUser(principal);
        cartItemRepository.deleteByUserId(user.getId());
        return Map.of("message", "장바구니가 비워졌습니다.");
    }

    private Map<String, Object> toResponse(CartItem item) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", item.getId());
        response.put("productId", item.getProduct().getId());
        response.put("productName", item.getProduct().getName());
        response.put("sellerId", item.getSeller().getId());
        response.put("sellerName", item.getSeller().getName());
        response.put("quantity", item.getQuantity());
        response.put("selectedOptions", readOptions(item.getSelectedOptionsJson()));
        response.put("unitPrice", resolvePrice(item));
        response.put("lineTotal", resolvePrice(item).multiply(BigDecimal.valueOf(item.getQuantity())));
        return response;
    }

    private BigDecimal resolvePrice(CartItem item) {
        return item.getProduct().getPriceEntries().stream()
                .filter(priceEntry -> priceEntry.getSeller().getId().equals(item.getSeller().getId()))
                .map(priceEntry -> priceEntry.getPrice())
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private String writeOptions(List<String> options) {
        try {
            return options == null || options.isEmpty() ? null : objectMapper.writeValueAsString(options);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "옵션 직렬화에 실패했습니다.");
        }
    }

    private List<String> readOptions(String optionsJson) {
        try {
            return optionsJson == null || optionsJson.isBlank()
                    ? List.of()
                    : objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {
                    });
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "옵션 역직렬화에 실패했습니다.");
        }
    }

    private User requireUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증 사용자를 찾을 수 없습니다."));
    }
}
