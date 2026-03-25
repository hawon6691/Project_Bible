package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class InquiryService(
    private val repository: InquiryRepository,
) {
    fun productInquiries(
        productId: Int,
        page: Int,
        limit: Int,
    ): StubResponse {
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listProductInquiries(productId, queryPage, queryLimit)
        return paged(result, queryPage, queryLimit)
    }

    fun createInquiry(
        userId: Int,
        productId: Int,
        request: InquiryRequest,
    ): StubResponse {
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
        validate(request.title, request.content)
        val created = repository.createInquiry(userId, NewInquiry(productId, request.title.trim(), request.content.trim(), request.isSecret))
        return StubResponse(status = HttpStatusCode.Created, data = payload(created))
    }

    fun answerInquiry(
        answeredBy: Int,
        inquiryId: Int,
        request: InquiryAnswerRequest,
    ): StubResponse {
        if (request.answer.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "answer는 비어 있을 수 없습니다.")
        }
        requireInquiry(inquiryId)
        return StubResponse(data = payload(repository.answerInquiry(inquiryId, answeredBy, request.answer.trim())))
    }

    fun myInquiries(
        userId: Int,
        page: Int,
        limit: Int,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listUserInquiries(userId, queryPage, queryLimit)
        return paged(result, queryPage, queryLimit)
    }

    fun deleteInquiry(
        userId: Int,
        inquiryId: Int,
    ): StubResponse {
        val inquiry = requireInquiry(inquiryId)
        if (inquiry.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "INQUIRY_FORBIDDEN", "본인 문의만 삭제할 수 있습니다.")
        }
        repository.deleteInquiry(inquiryId)
        return StubResponse(data = mapOf("message" to "Inquiry deleted."))
    }

    private fun requireInquiry(inquiryId: Int): InquiryRecord =
        repository.findInquiryById(inquiryId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "INQUIRY_NOT_FOUND", "문의를 찾을 수 없습니다.")

    private fun validate(
        title: String,
        content: String,
    ) {
        if (title.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title은 비어 있을 수 없습니다.")
        if (content.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.")
    }

    private fun paged(
        result: InquiryListResult,
        page: Int,
        limit: Int,
    ): StubResponse =
        StubResponse(
            data = result.items.map(::payload),
            meta =
                mapOf(
                    "page" to page,
                    "limit" to limit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + limit - 1) / limit),
                ),
        )

    private fun payload(record: InquiryRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "productId" to record.productId,
            "userId" to record.userId,
            "title" to record.title,
            "content" to record.content,
            "isSecret" to record.isSecret,
            "answer" to record.answer,
            "answeredBy" to record.answeredBy,
            "answeredAt" to record.answeredAt?.toString(),
            "status" to if (record.answer == null) "OPEN" else "ANSWERED",
            "createdAt" to record.createdAt.toString(),
        )
}
