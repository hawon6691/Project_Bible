package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

import java.time.Instant

data class CrawlerJobRecord(
    val id: Int,
    val sellerId: Int,
    val sellerName: String?,
    val name: String,
    val cronExpression: String?,
    val collectPrice: Boolean,
    val collectSpec: Boolean,
    val detectAnomaly: Boolean,
    val isActive: Boolean,
    val lastTriggeredAt: Instant?,
    val latestStatus: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CrawlerJobListResult(
    val items: List<CrawlerJobRecord>,
    val totalCount: Int,
)

data class CrawlerRunRecord(
    val id: Long,
    val jobId: Int,
    val jobName: String,
    val status: String,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val durationMs: Int?,
    val itemsProcessed: Int,
    val itemsCreated: Int,
    val itemsUpdated: Int,
    val itemsFailed: Int,
    val errorMessage: String?,
)

data class CrawlerRunListResult(
    val items: List<CrawlerRunRecord>,
    val totalCount: Int,
)

data class CrawlerMonitoringRecord(
    val totalJobs: Int,
    val activeJobs: Int,
    val recentRunCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val lastSuccessAt: Instant?,
)

data class NewCrawlerJob(
    val sellerId: Int,
    val name: String,
    val cronExpression: String?,
    val collectPrice: Boolean,
    val collectSpec: Boolean,
    val detectAnomaly: Boolean,
    val isActive: Boolean,
)

data class CrawlerJobUpdate(
    val sellerId: Int? = null,
    val name: String? = null,
    val cronExpression: String? = null,
    val collectPrice: Boolean? = null,
    val collectSpec: Boolean? = null,
    val detectAnomaly: Boolean? = null,
    val isActive: Boolean? = null,
)

interface CrawlerRepository {
    fun listJobs(
        status: String?,
        page: Int,
        limit: Int,
    ): CrawlerJobListResult

    fun findJobById(id: Int): CrawlerJobRecord?

    fun createJob(newJob: NewCrawlerJob): CrawlerJobRecord

    fun updateJob(
        id: Int,
        update: CrawlerJobUpdate,
    ): CrawlerJobRecord

    fun deleteJob(id: Int)

    fun triggerJob(id: Int): Pair<String, Long>

    fun triggerManual(
        jobId: Int?,
        sellerId: Int?,
        name: String?,
    ): Pair<String, Long>

    fun listRuns(
        status: String?,
        jobId: Int?,
        page: Int,
        limit: Int,
    ): CrawlerRunListResult

    fun monitoring(): CrawlerMonitoringRecord

    fun sellerExists(sellerId: Int): Boolean
}
