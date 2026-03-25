package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class AddressService(
    private val repository: AddressRepository,
) {
    fun list(userId: Int): StubResponse =
        StubResponse(data = repository.listAddresses(userId).map(::payload))

    fun create(
        userId: Int,
        request: AddressRequest,
    ): StubResponse {
        validate(request)
        val created =
            repository.createAddress(
                userId,
                NewAddress(
                    label = request.label.trim(),
                    recipientName = request.recipientName.trim(),
                    phone = request.phone.trim(),
                    zipCode = request.zipCode.trim(),
                    address = request.address.trim(),
                    addressDetail = request.addressDetail?.trim()?.takeIf { it.isNotBlank() },
                    isDefault = request.isDefault,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = payload(created))
    }

    fun update(
        userId: Int,
        addressId: Int,
        request: AddressUpdateRequest,
    ): StubResponse {
        if (
            request.label == null &&
            request.recipientName == null &&
            request.phone == null &&
            request.zipCode == null &&
            request.address == null &&
            request.addressDetail == null &&
            request.isDefault == null
        ) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 값이 없습니다.")
        }
        validate(request)
        val updated =
            repository.updateAddress(
                userId,
                addressId,
                AddressUpdate(
                    label = request.label?.trim(),
                    recipientName = request.recipientName?.trim(),
                    phone = request.phone?.trim(),
                    zipCode = request.zipCode?.trim(),
                    address = request.address?.trim(),
                    addressDetail = request.addressDetail?.trim(),
                    isDefault = request.isDefault,
                ),
            )
        return StubResponse(data = payload(updated))
    }

    fun delete(
        userId: Int,
        addressId: Int,
    ): StubResponse {
        requireAddress(userId, addressId)
        repository.deleteAddress(userId, addressId)
        return StubResponse(data = mapOf("message" to "Address deleted."))
    }

    private fun requireAddress(
        userId: Int,
        addressId: Int,
    ): AddressRecord =
        repository.findAddressById(userId, addressId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "ADDRESS_NOT_FOUND", "배송지를 찾을 수 없습니다.")

    private fun validate(request: AddressRequest) {
        validate(request.label, request.recipientName, request.phone, request.zipCode, request.address)
    }

    private fun validate(request: AddressUpdateRequest) {
        validate(
            request.label,
            request.recipientName,
            request.phone,
            request.zipCode,
            request.address,
        )
    }

    private fun validate(
        label: String?,
        recipientName: String?,
        phone: String?,
        zipCode: String?,
        address: String?,
    ) {
        if (label != null && label.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "label은 비어 있을 수 없습니다.")
        }
        if (recipientName != null && recipientName.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "recipientName은 비어 있을 수 없습니다.")
        }
        if (phone != null && phone.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "phone은 비어 있을 수 없습니다.")
        }
        if (zipCode != null && zipCode.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "zipCode는 비어 있을 수 없습니다.")
        }
        if (address != null && address.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "address는 비어 있을 수 없습니다.")
        }
    }

    private fun payload(record: AddressRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "label" to record.label,
            "recipientName" to record.recipientName,
            "phone" to record.phone,
            "zipCode" to record.zipCode,
            "address" to record.address,
            "addressDetail" to record.addressDetail,
            "isDefault" to record.isDefault,
            "createdAt" to record.createdAt.toString(),
        )
}
