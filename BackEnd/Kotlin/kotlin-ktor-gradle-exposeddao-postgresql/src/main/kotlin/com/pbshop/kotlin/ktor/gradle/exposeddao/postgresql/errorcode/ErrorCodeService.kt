package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse

class ErrorCodeService(
    private val repository: ErrorCodeRepository,
) {
    fun list(): StubResponse {
        val items = repository.listCodes().sortedBy { it.code }
        return StubResponse(data = mapOf("total" to items.size, "items" to items.map(::payload)))
    }

    fun detail(key: String): StubResponse = StubResponse(data = repository.findCode(key.trim()))

    private fun payload(record: ErrorCodeRecord): Map<String, Any?> =
        mapOf(
            "key" to record.key,
            "code" to record.code,
            "message" to record.message,
        )
}
