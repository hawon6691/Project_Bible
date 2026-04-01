package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class ResilienceService(
    private val repository: ResilienceRepository,
) {
    fun list(): StubResponse = StubResponse(data = mapOf("items" to repository.listSnapshots().map(::snapshotPayload)))

    fun policies(): StubResponse =
        StubResponse(
            data =
                mapOf(
                    "items" to
                        repository.getPolicies().map {
                            mapOf(
                                "name" to it.name,
                                "options" to mapOf("failureThreshold" to it.options.failureThreshold, "openTimeoutMs" to it.options.openTimeoutMs, "halfOpenSuccessThreshold" to it.options.halfOpenSuccessThreshold),
                                "stats" to it.stats,
                            )
                        },
                ),
        )

    fun detail(name: String): StubResponse {
        val snapshot =
            repository.getSnapshot(name)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "Circuit breaker를 찾을 수 없습니다.")
        return StubResponse(data = snapshotPayload(snapshot))
    }

    fun reset(name: String): StubResponse {
        val snapshot = repository.reset(name)
        return StubResponse(data = mapOf("message" to "Circuit breaker reset.", "name" to snapshot.name, "snapshot" to snapshotPayload(snapshot)))
    }

    fun listSnapshots(): List<CircuitBreakerSnapshotRecord> = repository.listSnapshots()

    fun listPolicies(): List<AdaptivePolicyRecord> = repository.getPolicies()

    private fun snapshotPayload(record: CircuitBreakerSnapshotRecord): Map<String, Any?> =
        mapOf(
            "name" to record.name,
            "status" to record.status,
            "failureCount" to record.failureCount,
            "successCount" to record.successCount,
            "nextAttemptAt" to record.nextAttemptAt,
            "lastFailureReason" to record.lastFailureReason,
            "options" to
                mapOf(
                    "failureThreshold" to record.options.failureThreshold,
                    "openTimeoutMs" to record.options.openTimeoutMs,
                    "halfOpenSuccessThreshold" to record.options.halfOpenSuccessThreshold,
                ),
        )
}
