package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal

import java.time.Instant

class InMemoryDealRepository private constructor(
    private val deals: MutableList<DealRecord>,
) : DealRepository {
    private var nextId = (deals.maxOfOrNull { it.id } ?: 0) + 1
    private var nextDealProductId = (deals.flatMap { it.products }.maxOfOrNull { it.id } ?: 0) + 1

    override fun listDeals(
        type: String?,
        page: Int,
        limit: Int,
    ): DealListResult {
        val filtered =
            deals
                .filter { type == null || it.type.equals(type, true) }
                .sortedByDescending { it.startAt }
        val offset = (page - 1) * limit
        return DealListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findDealById(id: Int): DealRecord? = deals.firstOrNull { it.id == id }

    override fun createDeal(newDeal: NewDeal): DealRecord {
        val now = Instant.now()
        val created =
            DealRecord(
                id = nextId++,
                productId = newDeal.productId,
                title = newDeal.title,
                description = newDeal.description,
                type = newDeal.type,
                discountRate = newDeal.discountRate,
                startAt = newDeal.startAt,
                endAt = newDeal.endAt,
                isActive = newDeal.isActive,
                bannerUrl = newDeal.bannerUrl,
                createdAt = now,
                updatedAt = now,
                products =
                    newDeal.products.map {
                        DealProductRecord(nextDealProductId++, it.productId, "상품 ${it.productId}", it.dealPrice, it.stock, 0)
                    },
            )
        deals += created
        return created
    }

    override fun updateDeal(
        id: Int,
        update: DealUpdate,
    ): DealRecord {
        val index = deals.indexOfFirst { it.id == id }
        check(index >= 0) { "Deal $id not found" }
        val current = deals[index]
        val updated =
            current.copy(
                title = update.title ?: current.title,
                description = update.description ?: current.description,
                type = update.type ?: current.type,
                discountRate = update.discountRate ?: current.discountRate,
                startAt = update.startAt ?: current.startAt,
                endAt = update.endAt ?: current.endAt,
                isActive = update.isActive ?: current.isActive,
                bannerUrl = update.bannerUrl ?: current.bannerUrl,
                updatedAt = Instant.now(),
                products =
                    update.products?.map {
                        DealProductRecord(nextDealProductId++, it.productId, "상품 ${it.productId}", it.dealPrice, it.stock, 0)
                    } ?: current.products,
            )
        deals[index] = updated
        return updated
    }

    override fun deleteDeal(id: Int) {
        deals.removeIf { it.id == id }
    }

    override fun productExists(productId: Int): Boolean = productId in setOf(1, 2, 3, 4, 5)

    companion object {
        fun seeded(): InMemoryDealRepository =
            InMemoryDealRepository(
                mutableListOf(
                    DealRecord(
                        id = 1,
                        productId = 1,
                        title = "봄맞이 노트북 특가전",
                        description = "최대 15% 할인",
                        type = "SPECIAL",
                        discountRate = 15,
                        startAt = Instant.now().minusSeconds(86_400),
                        endAt = Instant.now().plusSeconds(86_400 * 7),
                        isActive = true,
                        bannerUrl = "/uploads/deals/spring_sale.jpg",
                        createdAt = Instant.now().minusSeconds(86_400),
                        updatedAt = Instant.now().minusSeconds(43_200),
                        products = listOf(DealProductRecord(1, 1, "게이밍 노트북 A15", 1350000, 50, 10)),
                    ),
                ),
            )
    }
}
