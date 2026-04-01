package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import java.time.Instant

enum class AuctionStatus {
    OPEN,
    CLOSED,
    CANCELLED,
}

enum class AuctionBidStatus {
    ACTIVE,
    SELECTED,
    CANCELLED,
}

data class AuctionBidRecord(
    val id: Int,
    val auctionId: Int,
    val sellerId: Int,
    val price: Int,
    val description: String?,
    val deliveryDays: Int,
    val status: AuctionBidStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class AuctionSummaryRecord(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val categoryId: Int?,
    val budget: Int?,
    val status: AuctionStatus,
    val bidCount: Int,
    val createdAt: Instant,
)

data class AuctionDetailRecord(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val description: String,
    val categoryId: Int?,
    val specsJson: String?,
    val budget: Int?,
    val status: AuctionStatus,
    val bidCount: Int,
    val selectedBidId: Int?,
    val expiresAt: Instant?,
    val bids: List<AuctionBidRecord>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class AuctionPageResult(
    val items: List<AuctionSummaryRecord>,
    val totalCount: Int,
)

data class NewAuction(
    val title: String,
    val description: String,
    val categoryId: Int?,
    val specsJson: String?,
    val budget: Int?,
    val expiresAt: Instant?,
)

data class NewAuctionBid(
    val price: Int,
    val description: String?,
    val deliveryDays: Int,
)

data class AuctionBidUpdate(
    val price: Int?,
    val description: String?,
    val deliveryDays: Int?,
)

interface AuctionRepository {
    fun categoryExists(categoryId: Int): Boolean

    fun sellerExists(sellerId: Int): Boolean

    fun createAuction(ownerId: Int, input: NewAuction): AuctionDetailRecord

    fun listAuctions(status: AuctionStatus?, categoryId: Int?, page: Int, limit: Int): AuctionPageResult

    fun findAuctionById(id: Int): AuctionDetailRecord?

    fun createBid(sellerId: Int, auctionId: Int, input: NewAuctionBid): AuctionBidRecord

    fun updateBid(auctionId: Int, bidId: Int, update: AuctionBidUpdate): AuctionBidRecord?

    fun deleteBid(auctionId: Int, bidId: Int)

    fun selectBid(auctionId: Int, bidId: Int)

    fun cancelAuction(auctionId: Int)
}
