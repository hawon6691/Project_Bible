package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PointType
import io.ktor.http.HttpStatusCode
import java.time.Instant

class PointService(
    private val repository: PointRepository,
) {
    fun balance(userId: Int): StubResponse =
        repository.getBalance(userId).let {
            StubResponse(
                data =
                    mapOf(
                        "balance" to it.balance,
                        "expiringSoon" to it.expiringSoon,
                        "expiringDate" to it.expiringDate,
                    ),
            )
        }

    fun transactions(
        userId: Int,
        page: Int,
        limit: Int,
        type: String?,
    ): StubResponse {
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val parsedType = type?.let(::parseType)
        val result = repository.listTransactions(userId, queryPage, queryLimit, parsedType)
        return StubResponse(
            data = result.items.map(::payload),
            meta =
                mapOf(
                    "page" to queryPage,
                    "limit" to queryLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + queryLimit - 1) / queryLimit),
                ),
        )
    }

    fun grant(request: PointGrantRequest): StubResponse {
        if (request.amount <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "amount는 0보다 커야 합니다.")
        }
        if (!repository.userExists(request.userId)) {
            throw PbShopException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        }
        val created =
            repository.createTransaction(
                request.userId,
                NewPointTransaction(
                    type = PointType.ADMIN_GRANT,
                    amount = request.amount,
                    description = request.description.trim().ifBlank { "운영자 지급" },
                    referenceType = "ADMIN",
                    referenceId = 1,
                    expiresAt = request.expiresAt?.let(Instant::parse),
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = payload(created))
    }

    private fun parseType(value: String): PointType =
        runCatching { PointType.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 포인트 타입입니다.")
            }

    private fun payload(record: PointTransactionRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "type" to record.type.name,
            "amount" to record.amount,
            "balance" to record.balance,
            "description" to record.description,
            "referenceType" to record.referenceType,
            "referenceId" to record.referenceId,
            "expiresAt" to record.expiresAt?.toString(),
            "createdAt" to record.createdAt.toString(),
        )
}
