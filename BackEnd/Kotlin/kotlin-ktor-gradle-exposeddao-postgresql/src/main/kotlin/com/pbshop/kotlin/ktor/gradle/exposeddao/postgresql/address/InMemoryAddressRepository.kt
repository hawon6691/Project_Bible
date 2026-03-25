package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

import java.time.Instant

class InMemoryAddressRepository(
    seededAddresses: List<AddressRecord> = emptyList(),
) : AddressRepository {
    private val addresses = linkedMapOf<Int, AddressRecord>()
    private var nextId = 1

    init {
        seededAddresses.forEach {
            addresses[it.id] = it
            nextId = maxOf(nextId, it.id + 1)
        }
    }

    override fun listAddresses(userId: Int): List<AddressRecord> =
        addresses.values
            .filter { it.userId == userId }
            .sortedWith(compareByDescending<AddressRecord> { it.isDefault }.thenBy { it.id })

    override fun findAddressById(
        userId: Int,
        addressId: Int,
    ): AddressRecord? = addresses[addressId]?.takeIf { it.userId == userId }

    override fun createAddress(
        userId: Int,
        newAddress: NewAddress,
    ): AddressRecord {
        if (newAddress.isDefault) {
            unsetDefault(userId)
        }
        val created =
            AddressRecord(
                id = nextId++,
                userId = userId,
                label = newAddress.label,
                recipientName = newAddress.recipientName,
                phone = newAddress.phone,
                zipCode = newAddress.zipCode,
                address = newAddress.address,
                addressDetail = newAddress.addressDetail,
                isDefault = newAddress.isDefault || listAddresses(userId).isEmpty(),
                createdAt = Instant.now(),
            )
        addresses[created.id] = created
        return created
    }

    override fun updateAddress(
        userId: Int,
        addressId: Int,
        update: AddressUpdate,
    ): AddressRecord {
        val current = requireNotNull(findAddressById(userId, addressId)) { "Address $addressId not found" }
        if (update.isDefault == true) {
            unsetDefault(userId)
        }
        val updated =
            current.copy(
                label = update.label ?: current.label,
                recipientName = update.recipientName ?: current.recipientName,
                phone = update.phone ?: current.phone,
                zipCode = update.zipCode ?: current.zipCode,
                address = update.address ?: current.address,
                addressDetail = update.addressDetail ?: current.addressDetail,
                isDefault = update.isDefault ?: current.isDefault,
            )
        addresses[addressId] = updated
        return updated
    }

    override fun deleteAddress(
        userId: Int,
        addressId: Int,
    ) {
        val deleted = findAddressById(userId, addressId) ?: error("Address $addressId not found")
        addresses.remove(deleted.id)
        if (deleted.isDefault) {
            listAddresses(userId).firstOrNull()?.let { first -> addresses[first.id] = first.copy(isDefault = true) }
        }
    }

    private fun unsetDefault(userId: Int) {
        listAddresses(userId).forEach { address -> addresses[address.id] = address.copy(isDefault = false) }
    }

    companion object {
        fun seeded(): InMemoryAddressRepository =
            InMemoryAddressRepository(
                seededAddresses =
                    listOf(
                        AddressRecord(1, 4, "집", "홍길동", "01012345678", "06236", "서울시 강남구 테헤란로 123", "101동 1001호", true, Instant.now().minusSeconds(7200)),
                        AddressRecord(2, 4, "회사", "홍길동", "01012345678", "04782", "서울시 성동구 왕십리로 222", "20층", false, Instant.now().minusSeconds(3600)),
                        AddressRecord(3, 5, "집", "김영희", "01023456789", "48058", "부산시 해운대구 센텀중앙로 77", "1203호", true, Instant.now().minusSeconds(1800)),
                    ),
            )
    }
}
