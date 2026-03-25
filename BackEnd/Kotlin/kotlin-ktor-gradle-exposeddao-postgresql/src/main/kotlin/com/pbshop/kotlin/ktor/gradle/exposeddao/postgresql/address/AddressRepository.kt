package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

import java.time.Instant

data class AddressRecord(
    val id: Int,
    val userId: Int,
    val label: String,
    val recipientName: String,
    val phone: String,
    val zipCode: String,
    val address: String,
    val addressDetail: String?,
    val isDefault: Boolean,
    val createdAt: Instant,
)

data class NewAddress(
    val label: String,
    val recipientName: String,
    val phone: String,
    val zipCode: String,
    val address: String,
    val addressDetail: String?,
    val isDefault: Boolean,
)

data class AddressUpdate(
    val label: String? = null,
    val recipientName: String? = null,
    val phone: String? = null,
    val zipCode: String? = null,
    val address: String? = null,
    val addressDetail: String? = null,
    val isDefault: Boolean? = null,
)

interface AddressRepository {
    fun listAddresses(userId: Int): List<AddressRecord>

    fun findAddressById(
        userId: Int,
        addressId: Int,
    ): AddressRecord?

    fun createAddress(
        userId: Int,
        newAddress: NewAddress,
    ): AddressRecord

    fun updateAddress(
        userId: Int,
        addressId: Int,
        update: AddressUpdate,
    ): AddressRecord

    fun deleteAddress(
        userId: Int,
        addressId: Int,
    )
}
