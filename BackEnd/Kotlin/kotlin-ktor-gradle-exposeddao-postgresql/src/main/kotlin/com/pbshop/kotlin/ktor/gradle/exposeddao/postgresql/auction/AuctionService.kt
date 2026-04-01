package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode
import java.time.Instant

class AuctionService(
    private val repository: AuctionRepository,
) {
    fun createAuction(ownerId: Int, request: CreateAuctionRequest): StubResponse {
        validateAuctionRequest(request)
        request.categoryId?.let {
            if (!repository.categoryExists(it)) {
                throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "카테고리를 찾을 수 없습니다.")
            }
        }
        val created =
            repository.createAuction(
                ownerId = ownerId,
                input = NewAuction(request.title.trim(), request.description.trim(), request.categoryId, request.specs, request.budget, parseInstant(request.expiresAt)),
            )
        return StubResponse(status = HttpStatusCode.Created, data = detailPayload(created))
    }

    fun listAuctions(status: String?, categoryId: Int?, page: Int, limit: Int): StubResponse {
        val normalizedPage = if (page > 0) page else 1
        val normalizedLimit = limit.coerceIn(1, 100).takeIf { it > 0 } ?: 20
        val result = repository.listAuctions(parseStatus(status), categoryId, normalizedPage, normalizedLimit)
        return StubResponse(
            data = result.items.map(::summaryPayload),
            meta = mapOf("page" to normalizedPage, "limit" to normalizedLimit, "totalCount" to result.totalCount, "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount - 1) / normalizedLimit) + 1),
        )
    }

    fun detail(auctionId: Int): StubResponse = StubResponse(data = detailPayload(requireAuction(auctionId)))

    fun createBid(sellerId: Int, auctionId: Int, request: CreateAuctionBidRequest): StubResponse {
        requireOpenAuction(auctionId)
        if (!repository.sellerExists(sellerId)) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "판매자 권한이 필요합니다.")
        }
        if (request.price <= 0 || request.deliveryDays <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효한 입찰 정보를 입력해주세요.")
        }
        val bid = repository.createBid(sellerId, auctionId, NewAuctionBid(request.price, request.description?.trim()?.ifBlank { null }, request.deliveryDays))
        return StubResponse(status = HttpStatusCode.Created, data = bidPayload(bid))
    }

    fun selectBid(ownerId: Int, auctionId: Int, bidId: Int): StubResponse {
        val auction = requireOpenAuction(auctionId)
        if (auction.ownerId != ownerId) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "본인 경매에서만 낙찰을 선택할 수 있습니다.")
        }
        val bid = auction.bids.firstOrNull { it.id == bidId } ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "입찰을 찾을 수 없습니다.")
        if (bid.status != AuctionBidStatus.ACTIVE) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "활성 입찰만 선택할 수 있습니다.")
        }
        repository.selectBid(auctionId, bidId)
        return StubResponse(data = mapOf("message" to "낙찰이 선택되었습니다."))
    }

    fun cancelAuction(ownerId: Int, auctionId: Int): StubResponse {
        val auction = requireAuction(auctionId)
        if (auction.ownerId != ownerId) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "본인 경매만 취소할 수 있습니다.")
        }
        if (auction.status != AuctionStatus.OPEN) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "진행 중인 경매만 취소할 수 있습니다.")
        }
        repository.cancelAuction(auctionId)
        return StubResponse(data = mapOf("message" to "경매가 취소되었습니다."))
    }

    fun updateBid(sellerId: Int, auctionId: Int, bidId: Int, request: UpdateAuctionBidRequest): StubResponse {
        val auction = requireOpenAuction(auctionId)
        val bid = auction.bids.firstOrNull { it.id == bidId } ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "입찰을 찾을 수 없습니다.")
        if (bid.sellerId != sellerId) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "본인 입찰만 수정할 수 있습니다.")
        }
        if (request.price != null && request.price <= 0) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "price는 0보다 커야 합니다.")
        if (request.deliveryDays != null && request.deliveryDays <= 0) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "deliveryDays는 0보다 커야 합니다.")
        val updated = repository.updateBid(auctionId, bidId, AuctionBidUpdate(request.price, request.description?.trim(), request.deliveryDays))
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "입찰을 찾을 수 없습니다.")
        return StubResponse(data = bidPayload(updated))
    }

    fun deleteBid(sellerId: Int, auctionId: Int, bidId: Int): StubResponse {
        val auction = requireOpenAuction(auctionId)
        val bid = auction.bids.firstOrNull { it.id == bidId } ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "입찰을 찾을 수 없습니다.")
        if (bid.sellerId != sellerId) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "본인 입찰만 취소할 수 있습니다.")
        }
        repository.deleteBid(auctionId, bidId)
        return StubResponse(data = mapOf("message" to "입찰이 취소되었습니다."))
    }

    private fun requireAuction(auctionId: Int): AuctionDetailRecord =
        repository.findAuctionById(auctionId) ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "경매를 찾을 수 없습니다.")

    private fun requireOpenAuction(auctionId: Int): AuctionDetailRecord {
        val auction = requireAuction(auctionId)
        if (auction.status != AuctionStatus.OPEN) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "진행 중인 경매가 아닙니다.")
        }
        return auction
    }

    private fun validateAuctionRequest(request: CreateAuctionRequest) {
        if (request.title.trim().isBlank() || request.description.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title과 description이 필요합니다.")
        }
        if (request.budget != null && request.budget <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "budget은 0보다 커야 합니다.")
        }
        parseInstant(request.expiresAt)
    }

    private fun parseInstant(value: String?): Instant? =
        value?.trim()?.takeIf { it.isNotBlank() }?.let {
            runCatching { Instant.parse(it) }.getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "expiresAt 형식이 올바르지 않습니다.")
            }
        }

    private fun parseStatus(status: String?): AuctionStatus? =
        status?.trim()?.takeIf { it.isNotBlank() }?.uppercase()?.let {
            AuctionStatus.entries.firstOrNull { entry -> entry.name == it }
                ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "지원하지 않는 auction status 입니다.")
        }

    private fun summaryPayload(record: AuctionSummaryRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "ownerId" to record.ownerId,
            "title" to record.title,
            "categoryId" to record.categoryId,
            "budget" to record.budget,
            "status" to record.status.name,
            "bidCount" to record.bidCount,
            "createdAt" to record.createdAt.toString(),
        )

    private fun bidPayload(record: AuctionBidRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "auctionId" to record.auctionId,
            "sellerId" to record.sellerId,
            "price" to record.price,
            "description" to record.description,
            "deliveryDays" to record.deliveryDays,
            "status" to record.status.name,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun detailPayload(record: AuctionDetailRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "ownerId" to record.ownerId,
            "title" to record.title,
            "description" to record.description,
            "categoryId" to record.categoryId,
            "specs" to record.specsJson,
            "budget" to record.budget,
            "status" to record.status.name,
            "bidCount" to record.bidCount,
            "selectedBidId" to record.selectedBidId,
            "expiresAt" to record.expiresAt?.toString(),
            "bids" to record.bids.map(::bidPayload),
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )
}
