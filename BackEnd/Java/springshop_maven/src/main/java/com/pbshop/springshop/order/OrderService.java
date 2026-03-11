package com.pbshop.springshop.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.address.Address;
import com.pbshop.springshop.address.AddressRepository;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.cart.CartItem;
import com.pbshop.springshop.cart.CartItemRepository;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.order.dto.OrderDtos.CreateOrderItemRequest;
import com.pbshop.springshop.order.dto.OrderDtos.CreateOrderRequest;
import com.pbshop.springshop.order.dto.OrderDtos.UpdateOrderStatusRequest;
import com.pbshop.springshop.product.PriceEntry;
import com.pbshop.springshop.product.PriceEntryRepository;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.product.SellerRepository;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final PriceEntryRepository priceEntryRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderRepository orderRepository,
            AddressRepository addressRepository,
            ProductRepository productRepository,
            SellerRepository sellerRepository,
            PriceEntryRepository priceEntryRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.priceEntryRepository = priceEntryRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> createOrder(AuthenticatedUserPrincipal principal, CreateOrderRequest request) {
        User user = requireUser(principal);
        Address address = addressRepository.findByIdAndUserId(request.addressId(), user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "배송지를 찾을 수 없습니다."));

        List<OrderItemDraft> drafts = request.fromCart()
                ? loadDraftsFromCart(user)
                : loadDraftsFromRequest(request.items());

        if (drafts.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "주문할 상품이 없습니다.");
        }

        BigDecimal totalAmount = drafts.stream()
                .map(OrderItemDraft::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pointUsed = request.pointUsed() == null ? BigDecimal.ZERO : request.pointUsed();
        if (pointUsed.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "포인트는 0 이상이어야 합니다.");
        }
        if (pointUsed.compareTo(totalAmount) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "사용 포인트가 총 결제 금액보다 클 수 없습니다.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setStatus("CREATED");
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setRecipientName(address.getRecipientName());
        order.setRecipientPhone(address.getPhone());
        order.setZipCode(address.getZipCode());
        order.setAddress1(address.getAddress1());
        order.setAddress2(address.getAddress2());
        order.setDeliveryRequest(address.getDeliveryRequest());
        order.setMemo(request.memo());
        order.setPointUsed(pointUsed);
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount.subtract(pointUsed));

        drafts.forEach(draft -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(draft.product());
            item.setSeller(draft.seller());
            item.setProductName(draft.product().getName());
            item.setSellerName(draft.seller().getName());
            item.setUnitPrice(draft.unitPrice());
            item.setShippingFee(draft.shippingFee());
            item.setQuantity(draft.quantity());
            item.setSelectedOptionsJson(draft.selectedOptionsJson());
            item.setLineTotal(draft.lineTotal());
            order.getItems().add(item);
        });

        Order saved = orderRepository.save(order);
        if (request.fromCart()) {
            cartItemRepository.deleteByUserId(user.getId());
        }

        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrders(AuthenticatedUserPrincipal principal, int page, int limit) {
        User user = requireUser(principal);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> result = orderRepository.findByUserIdOrderByIdDesc(user.getId(), pageable);
        return toPageResponse(result, page, limit);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrderDetail(AuthenticatedUserPrincipal principal, Long orderId) {
        User user = requireUser(principal);
        Order order = orderRepository.findDetailByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));
        return toDetailResponse(order);
    }

    public Map<String, Object> cancelOrder(AuthenticatedUserPrincipal principal, Long orderId) {
        User user = requireUser(principal);
        Order order = orderRepository.findDetailByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if ("PAID".equalsIgnoreCase(order.getStatus()) || "REFUNDED".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "결제 완료 주문은 결제 환불 흐름을 이용해야 합니다.");
        }
        if ("CANCELED".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 취소된 주문입니다.");
        }

        order.setStatus("CANCELED");
        order.setCanceledAt(OffsetDateTime.now());
        return toDetailResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminOrders(AuthenticatedUserPrincipal principal, int page, int limit, String status) {
        requireAdmin(principal);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> result = orderRepository.findAllByOrderByIdDesc(pageable);
        if (status != null && !status.isBlank()) {
            List<Order> filtered = result.getContent().stream()
                    .filter(order -> status.equalsIgnoreCase(order.getStatus()))
                    .toList();
            return Map.of(
                    "items", filtered.stream().map(this::toSummaryResponse).toList(),
                    "pagination", Map.of(
                            "page", page,
                            "limit", limit,
                            "total", filtered.size(),
                            "totalPages", filtered.isEmpty() ? 0 : 1
                    )
            );
        }
        return toPageResponse(result, page, limit);
    }

    public Map<String, Object> updateOrderStatus(
            AuthenticatedUserPrincipal principal,
            Long orderId,
            UpdateOrderStatusRequest request
    ) {
        requireAdmin(principal);
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));
        order.setStatus(request.status());
        if ("CANCELED".equalsIgnoreCase(request.status()) && order.getCanceledAt() == null) {
            order.setCanceledAt(OffsetDateTime.now());
        }
        return toDetailResponse(orderRepository.save(order));
    }

    private Map<String, Object> toPageResponse(Page<Order> result, int page, int limit) {
        return Map.of(
                "items", result.getContent().stream().map(this::toSummaryResponse).toList(),
                "pagination", Map.of(
                        "page", page,
                        "limit", limit,
                        "total", result.getTotalElements(),
                        "totalPages", result.getTotalPages()
                )
        );
    }

    private Map<String, Object> toSummaryResponse(Order order) {
        return Map.of(
                "id", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus(),
                "itemCount", order.getItems().size(),
                "totalAmount", order.getTotalAmount(),
                "finalAmount", order.getFinalAmount(),
                "createdAt", order.getCreatedAt() == null ? "" : order.getCreatedAt().toString()
        );
    }

    private Map<String, Object> toDetailResponse(Order order) {
        Map<String, Object> address = new LinkedHashMap<>();
        address.put("id", order.getAddress().getId());
        address.put("recipientName", order.getRecipientName());
        address.put("phone", order.getRecipientPhone());
        address.put("zipCode", order.getZipCode());
        address.put("address1", order.getAddress1());
        address.put("address2", order.getAddress2() == null ? "" : order.getAddress2());
        address.put("deliveryRequest", order.getDeliveryRequest() == null ? "" : order.getDeliveryRequest());

        return Map.of(
                "id", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus(),
                "address", address,
                "memo", order.getMemo() == null ? "" : order.getMemo(),
                "pointUsed", order.getPointUsed(),
                "totalAmount", order.getTotalAmount(),
                "finalAmount", order.getFinalAmount(),
                "items", order.getItems().stream().map(this::toItemResponse).toList(),
                "createdAt", order.getCreatedAt() == null ? "" : order.getCreatedAt().toString()
        );
    }

    private Map<String, Object> toItemResponse(OrderItem item) {
        return Map.of(
                "id", item.getId(),
                "productId", item.getProduct().getId(),
                "productName", item.getProductName(),
                "sellerId", item.getSeller().getId(),
                "sellerName", item.getSellerName(),
                "unitPrice", item.getUnitPrice(),
                "shippingFee", item.getShippingFee(),
                "quantity", item.getQuantity(),
                "selectedOptions", readOptions(item.getSelectedOptionsJson()),
                "lineTotal", item.getLineTotal()
        );
    }

    private List<OrderItemDraft> loadDraftsFromCart(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByIdDesc(user.getId());
        return cartItems.stream()
                .map(item -> {
                    PriceEntry priceEntry = priceEntryRepository.findByProductIdAndSellerId(item.getProduct().getId(), item.getSeller().getId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 정보를 찾을 수 없습니다."));
                    BigDecimal lineTotal = priceEntry.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                            .add(priceEntry.getShippingFee());
                    return new OrderItemDraft(
                            item.getProduct(),
                            item.getSeller(),
                            item.getQuantity(),
                            item.getSelectedOptionsJson(),
                            priceEntry.getPrice(),
                            priceEntry.getShippingFee(),
                            lineTotal
                    );
                })
                .toList();
    }

    private List<OrderItemDraft> loadDraftsFromRequest(List<CreateOrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "주문 항목이 필요합니다.");
        }

        return items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.productId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
                    Seller seller = sellerRepository.findById(item.sellerId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "판매처를 찾을 수 없습니다."));
                    PriceEntry priceEntry = priceEntryRepository.findByProductIdAndSellerId(product.getId(), seller.getId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 정보를 찾을 수 없습니다."));
                    String optionsJson = writeOptions(item.selectedOptions());
                    BigDecimal lineTotal = priceEntry.getPrice().multiply(BigDecimal.valueOf(item.quantity()))
                            .add(priceEntry.getShippingFee());
                    return new OrderItemDraft(
                            product,
                            seller,
                            item.quantity(),
                            optionsJson,
                            priceEntry.getPrice(),
                            priceEntry.getShippingFee(),
                            lineTotal
                    );
                })
                .toList();
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

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private record OrderItemDraft(
            Product product,
            Seller seller,
            int quantity,
            String selectedOptionsJson,
            BigDecimal unitPrice,
            BigDecimal shippingFee,
            BigDecimal lineTotal
    ) {
    }
}
