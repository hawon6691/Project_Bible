package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.observability

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.CrawlerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard.OpsDashboardService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin.QueueAdminRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience.ResilienceService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.SearchRepository
import java.lang.management.ManagementFactory

class ObservabilityService(
    private val repository: ObservabilityRepository,
    private val queueAdminRepository: QueueAdminRepository,
    private val resilienceService: ResilienceService,
    private val searchRepository: SearchRepository,
    private val crawlerRepository: CrawlerRepository,
    private val opsDashboardService: OpsDashboardService,
) {
    fun metrics(): StubResponse {
        val summary = repository.getMetricsSummary()
        return StubResponse(data = metricsPayload(summary))
    }

    fun traces(limit: Int, pathContains: String?): StubResponse =
        StubResponse(
            data =
                mapOf(
                    "items" to
                        repository.listTraces(limit.coerceIn(1, 200), pathContains).map {
                            mapOf(
                                "requestId" to it.requestId,
                                "method" to it.method,
                                "path" to it.path,
                                "statusCode" to it.statusCode,
                                "durationMs" to it.durationMs,
                                "timestamp" to it.timestamp.toString(),
                                "role" to it.role,
                            )
                        },
                ),
        )

    fun dashboard(): StubResponse {
        val runtime = Runtime.getRuntime()
        val mxBean = ManagementFactory.getRuntimeMXBean()
        return StubResponse(
            data =
                mapOf(
                    "checkedAt" to java.time.Instant.now().toString(),
                    "process" to
                        mapOf(
                            "uptimeSec" to mxBean.uptime / 1000,
                            "memory" to
                                mapOf(
                                    "free" to runtime.freeMemory(),
                                    "total" to runtime.totalMemory(),
                                    "max" to runtime.maxMemory(),
                                ),
                        ),
                    "metrics" to metricsPayload(repository.getMetricsSummary()),
                    "queue" to queueAdminRepository.getStats(),
                    "resilience" to
                        mapOf(
                            "circuits" to resilienceService.listSnapshots().map(::snapshotPayload),
                            "adaptivePolicies" to resilienceService.listPolicies().map(::policyPayload),
                        ),
                    "searchSync" to searchRepository.getOutboxSummary(),
                    "crawler" to crawlerRepository.monitoring(),
                    "opsSummary" to opsDashboardService.summaryPayload(),
                ),
        )
    }

    private fun metricsPayload(record: MetricsSummaryRecord): Map<String, Any?> =
        mapOf(
            "totalRequests" to record.totalRequests,
            "errorRequests" to record.errorRequests,
            "errorRate" to record.errorRate,
            "avgLatencyMs" to record.avgLatencyMs,
            "p95LatencyMs" to record.p95LatencyMs,
            "p99LatencyMs" to record.p99LatencyMs,
            "statusBuckets" to record.statusBuckets,
        )

    private fun snapshotPayload(record: com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience.CircuitBreakerSnapshotRecord): Map<String, Any?> =
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

    private fun policyPayload(record: com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience.AdaptivePolicyRecord): Map<String, Any?> =
        mapOf(
            "name" to record.name,
            "options" to
                mapOf(
                    "failureThreshold" to record.options.failureThreshold,
                    "openTimeoutMs" to record.options.openTimeoutMs,
                    "halfOpenSuccessThreshold" to record.options.halfOpenSuccessThreshold,
                ),
            "stats" to record.stats,
        )
}
