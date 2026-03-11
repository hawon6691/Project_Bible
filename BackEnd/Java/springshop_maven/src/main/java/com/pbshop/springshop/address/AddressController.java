package com.pbshop.springshop.address;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.address.dto.AddressDtos.SaveAddressRequest;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAddresses(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(addressService.getAddresses(principal));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createAddress(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SaveAddressRequest request
    ) {
        return ApiResponse.success(addressService.createAddress(principal, request));
    }

    @PatchMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateAddress(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SaveAddressRequest request
    ) {
        return ApiResponse.success(addressService.updateAddress(principal, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> deleteAddress(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(addressService.deleteAddress(principal, id));
    }
}
