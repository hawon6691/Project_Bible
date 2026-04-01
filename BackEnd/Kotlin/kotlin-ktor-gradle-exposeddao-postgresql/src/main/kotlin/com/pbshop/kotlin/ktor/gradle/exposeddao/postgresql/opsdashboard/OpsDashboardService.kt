package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler.CrawlerRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin.QueueStatsRecord
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin.QueueAdminRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search.SearchRepository

class OpsDashboardService(
    private val repository: OpsDashboardRepository,
    private val dbHealthService: DbHealthService,
    private val searchRepository: SearchRepository,
    private val crawlerRepository: CrawlerRepository,
    private val queueAdminRepository: QueueAdminRepository,
) {
    fun summary(): StubResponse = StubResponse(data = summaryPayload())

    fun summaryPayload(): Map<String, Any?> {
        val errors = linkedMapOf<String, String>()
        val health = runCatching { healthPayload() }.getOrElse { errors["health"] = it.message ?: "Unknown error"; null }
        val searchSync = runCatching { searchPayload() }.getOrElse { errors["searchSync"] = it.message ?: "Unknown error"; null }
        val crawler = runCatching { crawlerPayload() }.getOrElse { errors["crawler"] = it.message ?: "Unknown error"; null }
        val queue = runCatching { queuePayload(queueAdminRepository.getStats()) }.getOrElse { errors["queue"] = it.message ?: "Unknown error"; null }
        return repository.buildSummary(health, searchSync, crawler, queue, errors)
    }

    private fun healthPayload(): Map<String, Any?> {
        val check = dbHealthService.check()
        return mapOf("status" to if (check.isUp) "up" else "down", "db" to mapOf("status" to check.status, "engine" to check.engine, "database" to check.database, "message" to check.message))
    }

    private fun searchPayload(): Map<String, Any?> {
        val summary = searchRepository.getOutboxSummary()
        return mapOf("total" to summary.total, "pending" to summary.pending, "processing" to summary.processing, "completed" to summary.completed, "failed" to summary.failed)
    }

    private fun crawlerPayload(): Map<String, Any?> {
        val monitoring = crawlerRepository.monitoring()
        return mapOf(
            "totalJobs" to monitoring.totalJobs,
            "activeJobs" to monitoring.activeJobs,
            "recentRunCount" to monitoring.recentRunCount,
            "successCount" to monitoring.successCount,
            "failedRuns" to monitoring.failedCount,
            "lastSuccessAt" to monitoring.lastSuccessAt?.toString(),
        )
    }

    private fun queuePayload(record: QueueStatsRecord): Map<String, Any?> =
        mapOf(
            "items" to
                record.items.map {
                    mapOf(
                        "queueName" to it.queueName,
                        "paused" to it.paused,
                        "counts" to
                            mapOf(
                                "waiting" to it.counts.waiting,
                                "active" to it.counts.active,
                                "completed" to it.counts.completed,
                                "failed" to it.counts.failed,
                            ),
                    )
                },
            "total" to record.total,
        )
}
