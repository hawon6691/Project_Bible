package com.pbshop.java.spring.maven.jpa.postgresql.address;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.address.dto.AddressDtos.SaveAddressRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAddresses(AuthenticatedUserPrincipal principal) {
        User user = requireUser(principal);
        return addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public Map<String, Object> createAddress(AuthenticatedUserPrincipal principal, SaveAddressRequest request) {
        User user = requireUser(principal);
        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefaultAddress(user.getId());
        }

        Address address = new Address();
        address.setUser(user);
        applyRequest(address, request);
        if (!Boolean.TRUE.equals(request.isDefault()) && addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(user.getId()).isEmpty()) {
            address.setDefaultAddress(true);
        }

        return toResponse(addressRepository.save(address));
    }

    public Map<String, Object> updateAddress(
            AuthenticatedUserPrincipal principal,
            Long id,
            SaveAddressRequest request
    ) {
        User user = requireUser(principal);
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "배송지를 찾을 수 없습니다."));
        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefaultAddress(user.getId());
        }
        applyRequest(address, request);
        return toResponse(addressRepository.save(address));
    }

    public Map<String, Object> deleteAddress(AuthenticatedUserPrincipal principal, Long id) {
        User user = requireUser(principal);
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "배송지를 찾을 수 없습니다."));
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);

        if (wasDefault) {
            addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(user.getId()).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setDefaultAddress(true);
                        addressRepository.save(next);
                    });
        }

        return Map.of("message", "배송지가 삭제되었습니다.");
    }

    private void applyRequest(Address address, SaveAddressRequest request) {
        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setZipCode(request.zipCode());
        address.setAddress1(request.address1());
        address.setAddress2(request.address2());
        address.setLabel(request.label());
        address.setDeliveryRequest(request.deliveryRequest());
        address.setDefaultAddress(Boolean.TRUE.equals(request.isDefault()));
    }

    private void clearDefaultAddress(Long userId) {
        addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId).forEach(address -> {
            if (address.isDefaultAddress()) {
                address.setDefaultAddress(false);
                addressRepository.save(address);
            }
        });
    }

    private Map<String, Object> toResponse(Address address) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", address.getId());
        response.put("recipientName", address.getRecipientName());
        response.put("phone", address.getPhone());
        response.put("zipCode", address.getZipCode());
        response.put("address1", address.getAddress1());
        response.put("address2", address.getAddress2() == null ? "" : address.getAddress2());
        response.put("label", address.getLabel() == null ? "" : address.getLabel());
        response.put("deliveryRequest", address.getDeliveryRequest() == null ? "" : address.getDeliveryRequest());
        response.put("isDefault", address.isDefaultAddress());
        return response;
    }

    private User requireUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증 사용자를 찾을 수 없습니다."));
    }
}
