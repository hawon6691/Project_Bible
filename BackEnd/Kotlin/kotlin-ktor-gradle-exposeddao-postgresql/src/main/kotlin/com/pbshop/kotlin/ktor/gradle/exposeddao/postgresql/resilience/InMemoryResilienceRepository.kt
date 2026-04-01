package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience

import java.time.Instant

class InMemoryResilienceRepository private constructor(
    private val snapshots: MutableMap<String, CircuitBreakerSnapshotRecord>,
    private val policies: MutableMap<String, AdaptivePolicyRecord>,
) : ResilienceRepository {
    override fun listSnapshots(): List<CircuitBreakerSnapshotRecord> = snapshots.values.sortedBy { it.name }

    override fun getPolicies(): List<AdaptivePolicyRecord> = policies.values.sortedBy { it.name }

    override fun getSnapshot(name: String): CircuitBreakerSnapshotRecord? = snapshots[name]

    override fun reset(name: String): CircuitBreakerSnapshotRecord {
        val current = snapshots[name] ?: defaultSnapshot(name)
        val reset =
            current.copy(
                status = "CLOSED",
                failureCount = 0,
                successCount = 0,
                nextAttemptAt = null,
                lastFailureReason = null,
            )
        snapshots[name] = reset
        return reset
    }

    companion object {
        fun seeded(): InMemoryResilienceRepository {
            val options = CircuitBreakerOptionsRecord(3, 5000, 2)
            return InMemoryResilienceRepository(
                snapshots =
                    mutableMapOf(
                        "payment-provider" to
                            CircuitBreakerSnapshotRecord(
                                name = "payment-provider",
                                status = "CLOSED",
                                failureCount = 0,
                                successCount = 12,
                                nextAttemptAt = null,
                                lastFailureReason = null,
                                options = options,
                            ),
                    ),
                policies =
                    mutableMapOf(
                        "payment-provider" to AdaptivePolicyRecord("payment-provider", options, mapOf("success" to 120, "failure" to 3, "lastTunedAt" to Instant.now().epochSecond.toInt())),
                    ),
            )
        }

        private fun defaultSnapshot(name: String): CircuitBreakerSnapshotRecord =
            CircuitBreakerSnapshotRecord(
                name = name,
                status = "CLOSED",
                failureCount = 0,
                successCount = 0,
                nextAttemptAt = null,
                lastFailureReason = null,
                options = CircuitBreakerOptionsRecord(3, 5000, 2),
            )
    }
}
