package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability

import java.time.Instant
import kotlin.math.ceil

data class HttpTraceRecord(
    val requestId: String,
    val method: String,
    val path: String,
    val statusCode: Int,
    val durationMs: Long,
    val timestamp: Instant,
    val role: String,
)

data class MetricsSummaryRecord(
    val totalRequests: Int,
    val errorRequests: Int,
    val errorRate: Double,
    val avgLatencyMs: Double,
    val p95LatencyMs: Long,
    val p99LatencyMs: Long,
    val statusBuckets: Map<String, Int>,
)

interface ObservabilityRepository {
    fun recordTrace(trace: HttpTraceRecord)

    fun listTraces(limit: Int, pathContains: String?): List<HttpTraceRecord>

    fun getMetricsSummary(windowMinutes: Int = 15): MetricsSummaryRecord
}

class InMemoryObservabilityRepository(
    private val maxBuffer: Int = 500,
) : ObservabilityRepository {
    private val traces = mutableListOf<HttpTraceRecord>()

    override fun recordTrace(trace: HttpTraceRecord) {
        synchronized(traces) {
            traces += trace
            if (traces.size > maxBuffer) {
                traces.subList(0, traces.size - maxBuffer).clear()
            }
        }
    }

    override fun listTraces(limit: Int, pathContains: String?): List<HttpTraceRecord> {
        val normalized = pathContains?.trim()?.lowercase()
        val snapshot =
            synchronized(traces) {
                traces.toList()
            }
        val filtered =
            if (normalized.isNullOrBlank()) {
                snapshot
            } else {
                snapshot.filter { it.path.lowercase().contains(normalized) }
            }
        return filtered.takeLast(limit.coerceAtLeast(1)).reversed()
    }

    override fun getMetricsSummary(windowMinutes: Int): MetricsSummaryRecord {
        val from = Instant.now().minusSeconds(windowMinutes.toLong() * 60)
        val windowed =
            synchronized(traces) {
                traces.filter { it.timestamp >= from }
            }
        val durations = windowed.map { it.durationMs }.sorted()
        val total = windowed.size
        val errorCount = windowed.count { it.statusCode >= 400 }
        return MetricsSummaryRecord(
            totalRequests = total,
            errorRequests = errorCount,
            errorRate = if (total == 0) 0.0 else (errorCount.toDouble() / total).let { kotlin.math.round(it * 10000) / 10000.0 },
            avgLatencyMs = if (total == 0) 0.0 else windowed.map { it.durationMs }.average(),
            p95LatencyMs = percentile(durations, 0.95),
            p99LatencyMs = percentile(durations, 0.99),
            statusBuckets =
                mapOf(
                    "s2xx" to windowed.count { it.statusCode in 200..299 },
                    "s3xx" to windowed.count { it.statusCode in 300..399 },
                    "s4xx" to windowed.count { it.statusCode in 400..499 },
                    "s5xx" to windowed.count { it.statusCode >= 500 },
                ),
        )
    }

    private fun percentile(sorted: List<Long>, p: Double): Long {
        if (sorted.isEmpty()) {
            return 0
        }
        val idx = (ceil(sorted.size * p).toInt() - 1).coerceIn(0, sorted.lastIndex)
        return sorted[idx]
    }
}

object ObservabilityRuntimeRegistry {
    @Volatile
    var repository: ObservabilityRepository? = null
}
