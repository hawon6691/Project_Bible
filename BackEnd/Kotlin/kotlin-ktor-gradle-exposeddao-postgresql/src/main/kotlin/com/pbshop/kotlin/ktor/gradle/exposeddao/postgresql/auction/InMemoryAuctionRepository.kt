package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import java.time.Instant
import java.time.temporal.ChronoUnit

class InMemoryAuctionRepository(
    private val categoryIds: Set<Int>,
    private val sellerIds: Set<Int>,
    private val auctions: MutableList<AuctionDetailRecord>,
) : AuctionRepository {
    private var nextAuctionId = (auctions.maxOfOrNull { it.id } ?: 0) + 1
    private var nextBidId = (auctions.flatMap { it.bids }.maxOfOrNull { it.id } ?: 0) + 1

    override fun categoryExists(categoryId: Int): Boolean = categoryId in categoryIds

    override fun sellerExists(sellerId: Int): Boolean = sellerId in sellerIds

    override fun createAuction(ownerId: Int, input: NewAuction): AuctionDetailRecord {
        val now = Instant.now()
        val created =
            AuctionDetailRecord(
                id = nextAuctionId++,
                ownerId = ownerId,
                title = input.title,
                description = input.description,
                categoryId = input.categoryId,
                specsJson = input.specsJson,
                budget = input.budget,
                status = AuctionStatus.OPEN,
                bidCount = 0,
                selectedBidId = null,
                expiresAt = input.expiresAt,
                bids = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
        auctions += created
        return created
    }

    override fun listAuctions(status: AuctionStatus?, categoryId: Int?, page: Int, limit: Int): AuctionPageResult {
        val filtered =
            auctions
                .asSequence()
                .filter { status == null || it.status == status }
                .filter { categoryId == null || it.categoryId == categoryId }
                .sortedByDescending { it.createdAt }
                .map {
                    AuctionSummaryRecord(
                        it.id,
                        it.ownerId,
                        it.title,
                        it.categoryId,
                        it.budget,
                        it.status,
                        it.bids.count { bid -> bid.status != AuctionBidStatus.CANCELLED },
                        it.createdAt,
                    )
                }.toList()
        val from = ((page - 1) * limit).coerceAtMost(filtered.size)
        val to = (from + limit).coerceAtMost(filtered.size)
        return AuctionPageResult(filtered.subList(from, to), filtered.size)
    }

    override fun findAuctionById(id: Int): AuctionDetailRecord? = auctions.firstOrNull { it.id == id }

    override fun createBid(sellerId: Int, auctionId: Int, input: NewAuctionBid): AuctionBidRecord {
        val bid =
            AuctionBidRecord(
                id = nextBidId++,
                auctionId = auctionId,
                sellerId = sellerId,
                price = input.price,
                description = input.description,
                deliveryDays = input.deliveryDays,
                status = AuctionBidStatus.ACTIVE,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
        updateAuction(auctionId) { auction ->
            auction.copy(
                bidCount = auction.bidCount + 1,
                bids = auction.bids + bid,
                updatedAt = Instant.now(),
            )
        }
        return bid
    }

    override fun updateBid(auctionId: Int, bidId: Int, update: AuctionBidUpdate): AuctionBidRecord? {
        var updatedRecord: AuctionBidRecord? = null
        updateAuction(auctionId) { auction ->
            auction.copy(
                bids =
                    auction.bids.map { bid ->
                        if (bid.id == bidId) {
                            bid.copy(
                                price = update.price ?: bid.price,
                                description = update.description ?: bid.description,
                                deliveryDays = update.deliveryDays ?: bid.deliveryDays,
                                updatedAt = Instant.now(),
                            ).also { updatedRecord = it }
                        } else {
                            bid
                        }
                    },
                updatedAt = Instant.now(),
            )
        }
        return updatedRecord
    }

    override fun deleteBid(auctionId: Int, bidId: Int) {
        updateAuction(auctionId) { auction ->
            val remaining = auction.bids.filterNot { it.id == bidId }
            auction.copy(
                bidCount = remaining.count { it.status != AuctionBidStatus.CANCELLED },
                bids = remaining,
                updatedAt = Instant.now(),
            )
        }
    }

    override fun selectBid(auctionId: Int, bidId: Int) {
        updateAuction(auctionId) { auction ->
            auction.copy(
                status = AuctionStatus.CLOSED,
                selectedBidId = bidId,
                bids =
                    auction.bids.map { bid ->
                        when (bid.id) {
                            bidId -> bid.copy(status = AuctionBidStatus.SELECTED, updatedAt = Instant.now())
                            else -> bid
                        }
                    },
                updatedAt = Instant.now(),
            )
        }
    }

    override fun cancelAuction(auctionId: Int) {
        updateAuction(auctionId) { auction -> auction.copy(status = AuctionStatus.CANCELLED, updatedAt = Instant.now()) }
    }

    private fun updateAuction(auctionId: Int, transform: (AuctionDetailRecord) -> AuctionDetailRecord) {
        val index = auctions.indexOfFirst { it.id == auctionId }
        if (index >= 0) {
            auctions[index] = transform(auctions[index])
        }
    }

    companion object {
        fun seeded(): InMemoryAuctionRepository {
            val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)
            return InMemoryAuctionRepository(
                categoryIds = setOf(1, 2, 3, 4, 5),
                sellerIds = setOf(1, 2, 3),
                auctions =
                    mutableListOf(
                        AuctionDetailRecord(
                            id = 1,
                            ownerId = 4,
                            title = "게이밍 노트북 구매 요청",
                            description = "150만원대 견적을 받고 싶습니다.",
                            categoryId = 2,
                            specsJson = """{"CPU":"i7","RAM":"16GB"}""",
                            budget = 1500000,
                            status = AuctionStatus.OPEN,
                            bidCount = 1,
                            selectedBidId = null,
                            expiresAt = now.plus(3, ChronoUnit.DAYS),
                            bids =
                                listOf(
                                    AuctionBidRecord(
                                        id = 1,
                                        auctionId = 1,
                                        sellerId = 2,
                                        price = 1420000,
                                        description = "3일 내 배송 가능합니다.",
                                        deliveryDays = 3,
                                        status = AuctionBidStatus.ACTIVE,
                                        createdAt = now.minus(1, ChronoUnit.DAYS),
                                        updatedAt = now.minus(1, ChronoUnit.DAYS),
                                    ),
                                ),
                            createdAt = now.minus(2, ChronoUnit.DAYS),
                            updatedAt = now.minus(1, ChronoUnit.DAYS),
                        ),
                    ),
            )
        }
    }
}
