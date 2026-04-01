package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience

data class CircuitBreakerOptionsRecord(
    val failureThreshold: Int,
    val openTimeoutMs: Int,
    val halfOpenSuccessThreshold: Int,
)

data class CircuitBreakerSnapshotRecord(
    val name: String,
    val status: String,
    val failureCount: Int,
    val successCount: Int,
    val nextAttemptAt: String?,
    val lastFailureReason: String?,
    val options: CircuitBreakerOptionsRecord,
)

data class AdaptivePolicyRecord(
    val name: String,
    val options: CircuitBreakerOptionsRecord,
    val stats: Map<String, Int>,
)

interface ResilienceRepository {
    fun listSnapshots(): List<CircuitBreakerSnapshotRecord>

    fun getPolicies(): List<AdaptivePolicyRecord>

    fun getSnapshot(name: String): CircuitBreakerSnapshotRecord?

    fun reset(name: String): CircuitBreakerSnapshotRecord
}
