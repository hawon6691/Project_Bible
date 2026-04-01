package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard

class InMemoryOpsDashboardRepository(
    private val searchFailedThreshold: Int = 1,
    private val crawlerFailedRunsThreshold: Int = 1,
    private val queueFailedThreshold: Int = 1,
) : OpsDashboardRepository {
    override fun buildSummary(
        health: Map<String, Any?>?,
        searchSync: Map<String, Any?>?,
        crawler: Map<String, Any?>?,
        queue: Map<String, Any?>?,
        errors: Map<String, String>,
    ): Map<String, Any?> {
        val alerts = mutableListOf<Map<String, String>>()
        val searchFailed = (searchSync?.get("failed") as? Int) ?: 0
        val crawlerFailedRuns = (crawler?.get("failedRuns") as? Int) ?: 0
        val queueFailed =
            (((queue?.get("items") as? List<*>)?.sumOf { (((it as? Map<*, *>)?.get("counts") as? Map<*, *>)?.get("failed") as? Int) ?: 0 })) ?: 0

        if ((health?.get("status") as? String) == "down") {
            alerts += mapOf("key" to "health", "severity" to "critical", "message" to "헬스 상태가 비정상입니다.")
        }
        if (searchFailed >= searchFailedThreshold && searchFailedThreshold > 0) {
            alerts += mapOf("key" to "searchSync", "severity" to "warning", "message" to "검색 동기화 실패 건이 임계치를 초과했습니다.")
        }
        if (crawlerFailedRuns >= crawlerFailedRunsThreshold && crawlerFailedRunsThreshold > 0) {
            alerts += mapOf("key" to "crawler", "severity" to "warning", "message" to "크롤러 실패 실행 건이 임계치를 초과했습니다.")
        }
        if (queueFailed >= queueFailedThreshold && queueFailedThreshold > 0) {
            alerts += mapOf("key" to "queue", "severity" to "warning", "message" to "실패 Job이 있는 큐가 임계치를 초과했습니다.")
        }
        if (errors.isNotEmpty()) {
            alerts += mapOf("key" to "partial_failure", "severity" to "critical", "message" to "일부 지표 수집에 실패했습니다.")
        }

        return mapOf(
            "checkedAt" to java.time.Instant.now().toString(),
            "overallStatus" to if (errors.isEmpty()) "up" else "degraded",
            "health" to health,
            "searchSync" to searchSync,
            "crawler" to crawler,
            "queue" to queue,
            "errors" to errors,
            "alerts" to alerts,
            "alertCount" to alerts.size,
        )
    }
}
