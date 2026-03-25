package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import java.time.Instant

data class SellerRecord(
    val id: Int,
    val name: String,
    val url: String,
    val logoUrl: String?,
    val trustScore: Int,
    val trustGrade: String?,
    val description: String?,
    val isActive: Boolean,
    val createdAt: Instant,
)

data class SellerListResult(
    val items: List<SellerRecord>,
    val totalCount: Int,
)

data class NewSeller(
    val name: String,
    val url: String,
    val logoUrl: String?,
    val trustScore: Int,
    val trustGrade: String?,
    val description: String?,
    val isActive: Boolean,
)

data class SellerUpdate(
    val name: String?,
    val url: String?,
    val logoUrl: String?,
    val trustScore: Int?,
    val trustGrade: String?,
    val description: String?,
    val isActive: Boolean?,
)

interface SellerRepository {
    fun listSellers(
        page: Int,
        limit: Int,
    ): SellerListResult

    fun findSellerById(id: Int): SellerRecord?

    fun createSeller(newSeller: NewSeller): SellerRecord

    fun updateSeller(
        id: Int,
        update: SellerUpdate,
    ): SellerRecord

    fun deleteSeller(id: Int)

    fun hasLinkedPriceEntries(id: Int): Boolean
}
