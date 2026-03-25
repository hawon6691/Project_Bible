package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class SellerService(
    private val repository: SellerRepository,
) {
    fun listSellers(
        page: Int,
        limit: Int,
    ): StubResponse {
        val normalizedPage = if (page < 1) 1 else page
        val normalizedLimit = limit.coerceIn(1, 100)
        val result = repository.listSellers(normalizedPage, normalizedLimit)
        return StubResponse(
            data = result.items.map(::sellerPayload),
            meta =
                mapOf(
                    "page" to normalizedPage,
                    "limit" to normalizedLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + normalizedLimit - 1) / normalizedLimit),
                ),
        )
    }

    fun detail(id: Int): StubResponse =
        StubResponse(data = sellerPayload(requireSeller(id)))

    fun create(request: SellerRequest): StubResponse {
        validateRequest(request)
        val created =
            repository.createSeller(
                NewSeller(
                    name = request.name.trim(),
                    url = request.url.trim(),
                    logoUrl = request.logoUrl?.trim()?.takeIf { it.isNotBlank() },
                    trustScore = request.trustScore.coerceIn(0, 100),
                    trustGrade = request.trustGrade?.trim()?.takeIf { it.isNotBlank() },
                    description = request.description?.trim()?.takeIf { it.isNotBlank() },
                    isActive = request.isActive,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = sellerPayload(created))
    }

    fun update(
        id: Int,
        request: SellerUpdateRequest,
    ): StubResponse {
        if (
            request.name == null &&
            request.url == null &&
            request.logoUrl == null &&
            request.trustScore == null &&
            request.trustGrade == null &&
            request.description == null &&
            request.isActive == null
        ) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 판매처 값이 없습니다.")
        }
        request.name?.let(::validateName)
        request.url?.let(::validateUrl)
        request.trustScore?.takeIf { it !in 0..100 }?.let {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "trustScore는 0 이상 100 이하여야 합니다.")
        }
        val updated =
            repository.updateSeller(
                id,
                SellerUpdate(
                    name = request.name?.trim(),
                    url = request.url?.trim(),
                    logoUrl = request.logoUrl?.trim(),
                    trustScore = request.trustScore,
                    trustGrade = request.trustGrade?.trim(),
                    description = request.description?.trim(),
                    isActive = request.isActive,
                ),
            )
        return StubResponse(data = sellerPayload(updated))
    }

    fun delete(id: Int): StubResponse {
        if (repository.hasLinkedPriceEntries(id)) {
            throw PbShopException(HttpStatusCode.Conflict, "SELLER_IN_USE", "가격 정보가 연결된 판매처는 삭제할 수 없습니다.")
        }
        repository.deleteSeller(id)
        return StubResponse(data = mapOf("message" to "판매처가 삭제되었습니다."))
    }

    private fun requireSeller(id: Int): SellerRecord =
        repository.findSellerById(id)
            ?: throw PbShopException(HttpStatusCode.NotFound, "SELLER_NOT_FOUND", "판매처를 찾을 수 없습니다.")

    private fun validateRequest(request: SellerRequest) {
        validateName(request.name)
        validateUrl(request.url)
        if (request.trustScore !in 0..100) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "trustScore는 0 이상 100 이하여야 합니다.")
        }
    }

    private fun validateName(name: String) {
        val normalized = name.trim()
        if (normalized.isBlank() || normalized.length > 100) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "판매처명은 1자 이상 100자 이하로 입력해주세요.")
        }
    }

    private fun validateUrl(url: String) {
        if (url.trim().isBlank() || !url.trim().startsWith("http")) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효한 판매처 URL이 필요합니다.")
        }
    }

    private fun sellerPayload(record: SellerRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "name" to record.name,
            "url" to record.url,
            "logoUrl" to record.logoUrl,
            "trustScore" to record.trustScore,
            "trustGrade" to record.trustGrade,
            "description" to record.description,
            "isActive" to record.isActive,
            "createdAt" to record.createdAt.toString(),
        )
}
