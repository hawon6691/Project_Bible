package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal

import java.time.Instant

data class DealProductRecord(
    val id: Int,
    val productId: Int,
    val productName: String,
    val dealPrice: Int,
    val stock: Int,
    val soldCount: Int,
)

data class DealRecord(
    val id: Int,
    val productId: Int,
    val title: String,
    val description: String?,
    val type: String,
    val discountRate: Int,
    val startAt: Instant,
    val endAt: Instant,
    val isActive: Boolean,
    val bannerUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val products: List<DealProductRecord> = emptyList(),
)

data class DealListResult(
    val items: List<DealRecord>,
    val totalCount: Int,
)

data class NewDealProduct(
    val productId: Int,
    val dealPrice: Int,
    val stock: Int,
)

data class NewDeal(
    val productId: Int,
    val title: String,
    val description: String?,
    val type: String,
    val discountRate: Int,
    val startAt: Instant,
    val endAt: Instant,
    val isActive: Boolean,
    val bannerUrl: String?,
    val products: List<NewDealProduct>,
)

data class DealUpdate(
    val title: String?,
    val description: String?,
    val type: String?,
    val discountRate: Int?,
    val startAt: Instant?,
    val endAt: Instant?,
    val isActive: Boolean?,
    val bannerUrl: String?,
    val products: List<NewDealProduct>?,
)

interface DealRepository {
    fun listDeals(
        type: String?,
        page: Int,
        limit: Int,
    ): DealListResult

    fun findDealById(id: Int): DealRecord?

    fun createDeal(newDeal: NewDeal): DealRecord

    fun updateDeal(
        id: Int,
        update: DealUpdate,
    ): DealRecord

    fun deleteDeal(id: Int)

    fun productExists(productId: Int): Boolean
}
