package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.AddressEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.AddressesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoAddressRepository(
    private val databaseFactory: DatabaseFactory,
) : AddressRepository {
    override fun listAddresses(userId: Int): List<AddressRecord> =
        databaseFactory.withTransaction {
            AddressesTable
                .selectAll()
                .where { AddressesTable.user eq userId }
                .orderBy(AddressesTable.isDefault to SortOrder.DESC, AddressesTable.id to SortOrder.ASC)
                .map(::toRecord)
        }

    override fun findAddressById(
        userId: Int,
        addressId: Int,
    ): AddressRecord? =
        databaseFactory.withTransaction {
            AddressesTable
                .selectAll()
                .where { (AddressesTable.id eq addressId) and (AddressesTable.user eq userId) }
                .limit(1)
                .firstOrNull()
                ?.let(::toRecord)
        }

    override fun createAddress(
        userId: Int,
        newAddress: NewAddress,
    ): AddressRecord =
        databaseFactory.withTransaction {
            if (newAddress.isDefault) {
                clearDefault(userId)
            }
            val created =
                AddressEntity.new {
                    this.userId = EntityID(userId, UsersTable)
                    label = newAddress.label
                    recipientName = newAddress.recipientName
                    phone = newAddress.phone
                    zipCode = newAddress.zipCode
                    addressLine = newAddress.address
                    addressDetail = newAddress.addressDetail
                    isDefault = newAddress.isDefault || listAddresses(userId).isEmpty()
                    createdAt = Instant.now()
                    updatedAt = Instant.now()
                }
            requireNotNull(findAddressById(userId, created.id.value))
        }

    override fun updateAddress(
        userId: Int,
        addressId: Int,
        update: AddressUpdate,
    ): AddressRecord =
        databaseFactory.withTransaction {
            if (update.isDefault == true) {
                clearDefault(userId)
            }
            val entity = AddressEntity.findById(addressId)?.takeIf { it.userId.value == userId } ?: error("Address $addressId not found")
            entity.apply {
                label = update.label ?: label
                recipientName = update.recipientName ?: recipientName
                phone = update.phone ?: phone
                zipCode = update.zipCode ?: zipCode
                addressLine = update.address ?: addressLine
                addressDetail = update.addressDetail ?: addressDetail
                isDefault = update.isDefault ?: isDefault
                updatedAt = Instant.now()
            }
            requireNotNull(findAddressById(userId, addressId))
        }

    override fun deleteAddress(
        userId: Int,
        addressId: Int,
    ) {
        databaseFactory.withTransaction {
            val entity = AddressEntity.findById(addressId)?.takeIf { it.userId.value == userId } ?: error("Address $addressId not found")
            val wasDefault = entity.isDefault
            entity.delete()
            if (wasDefault) {
                AddressEntity.find { AddressesTable.user eq userId }
                    .limit(1)
                    .firstOrNull()
                    ?.apply {
                        isDefault = true
                        updatedAt = Instant.now()
                    }
            }
        }
    }

    private fun clearDefault(userId: Int) {
        AddressEntity.find { AddressesTable.user eq userId }.forEach {
            it.isDefault = false
            it.updatedAt = Instant.now()
        }
    }

    private fun toRecord(row: org.jetbrains.exposed.sql.ResultRow): AddressRecord =
        AddressRecord(
            id = row[AddressesTable.id].value,
            userId = row[AddressesTable.user].value,
            label = row[AddressesTable.label],
            recipientName = row[AddressesTable.recipientName],
            phone = row[AddressesTable.phone],
            zipCode = row[AddressesTable.zipCode],
            address = row[AddressesTable.addressLine],
            addressDetail = row[AddressesTable.addressDetail],
            isDefault = row[AddressesTable.isDefault],
            createdAt = row[AddressesTable.createdAt],
        )
}
