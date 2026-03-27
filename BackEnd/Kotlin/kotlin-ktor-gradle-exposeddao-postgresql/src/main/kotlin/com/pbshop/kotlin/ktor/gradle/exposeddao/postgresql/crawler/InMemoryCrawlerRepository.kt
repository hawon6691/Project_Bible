package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class InMemoryCrawlerRepository private constructor() : CrawlerRepository {
    private val jobs = linkedMapOf<Int, CrawlerJobRecord>()
    private val runs = linkedMapOf<Long, CrawlerRunRecord>()
    private val nextJobId = AtomicInteger(2)
    private val nextRunId = AtomicLong(2)

    init {
        val now = Instant.now()
        jobs[1] =
            CrawlerJobRecord(
                id = 1,
                sellerId = 1,
                sellerName = "PB 공식몰",
                name = "PB 공식몰 가격 수집",
                cronExpression = "0 */6 * * *",
                collectPrice = true,
                collectSpec = true,
                detectAnomaly = true,
                isActive = true,
                lastTriggeredAt = now.minusSeconds(3600),
                latestStatus = "SUCCESS",
                createdAt = now.minusSeconds(86_400),
                updatedAt = now.minusSeconds(3600),
            )
        runs[1] =
            CrawlerRunRecord(
                id = 1,
                jobId = 1,
                jobName = "PB 공식몰 가격 수집",
                status = "SUCCESS",
                startedAt = now.minusSeconds(3700),
                finishedAt = now.minusSeconds(3600),
                durationMs = 95_000,
                itemsProcessed = 124,
                itemsCreated = 2,
                itemsUpdated = 35,
                itemsFailed = 0,
                errorMessage = null,
            )
    }

    override fun listJobs(
        status: String?,
        page: Int,
        limit: Int,
    ): CrawlerJobListResult {
        val filtered = jobs.values.filter { status == null || it.latestStatus.equals(status, true) }
        val offset = (page - 1) * limit
        return CrawlerJobListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findJobById(id: Int): CrawlerJobRecord? = jobs[id]

    override fun createJob(newJob: NewCrawlerJob): CrawlerJobRecord {
        val now = Instant.now()
        val id = nextJobId.getAndIncrement()
        val created =
            CrawlerJobRecord(
                id = id,
                sellerId = newJob.sellerId,
                sellerName = sellerName(newJob.sellerId),
                name = newJob.name,
                cronExpression = newJob.cronExpression,
                collectPrice = newJob.collectPrice,
                collectSpec = newJob.collectSpec,
                detectAnomaly = newJob.detectAnomaly,
                isActive = newJob.isActive,
                lastTriggeredAt = null,
                latestStatus = "IDLE",
                createdAt = now,
                updatedAt = now,
            )
        jobs[id] = created
        return created
    }

    override fun updateJob(
        id: Int,
        update: CrawlerJobUpdate,
    ): CrawlerJobRecord {
        val current = requireNotNull(jobs[id])
        val updated =
            current.copy(
                sellerId = update.sellerId ?: current.sellerId,
                sellerName = sellerName(update.sellerId ?: current.sellerId),
                name = update.name ?: current.name,
                cronExpression = update.cronExpression ?: current.cronExpression,
                collectPrice = update.collectPrice ?: current.collectPrice,
                collectSpec = update.collectSpec ?: current.collectSpec,
                detectAnomaly = update.detectAnomaly ?: current.detectAnomaly,
                isActive = update.isActive ?: current.isActive,
                updatedAt = Instant.now(),
            )
        jobs[id] = updated
        return updated
    }

    override fun deleteJob(id: Int) {
        jobs.remove(id)
    }

    override fun triggerJob(id: Int): Pair<String, Long> {
        val job = requireNotNull(jobs[id])
        val runId = nextRunId.getAndIncrement()
        val now = Instant.now()
        jobs[id] = job.copy(lastTriggeredAt = now, latestStatus = "SUCCESS", updatedAt = now)
        runs[runId] =
            CrawlerRunRecord(
                id = runId,
                jobId = id,
                jobName = job.name,
                status = "SUCCESS",
                startedAt = now.minusSeconds(5),
                finishedAt = now,
                durationMs = 5000,
                itemsProcessed = 12,
                itemsCreated = 1,
                itemsUpdated = 3,
                itemsFailed = 0,
                errorMessage = null,
            )
        return "Crawler job queued." to runId
    }

    override fun triggerManual(
        jobId: Int?,
        sellerId: Int?,
        name: String?,
    ): Pair<String, Long> {
        val targetJobId = jobId ?: jobs.values.firstOrNull { sellerId == null || it.sellerId == sellerId }?.id ?: 1
        return triggerJob(targetJobId)
    }

    override fun listRuns(
        status: String?,
        jobId: Int?,
        page: Int,
        limit: Int,
    ): CrawlerRunListResult {
        val filtered =
            runs.values.filter {
                (status == null || it.status.equals(status, true)) &&
                    (jobId == null || it.jobId == jobId)
            }.sortedByDescending { it.startedAt }
        val offset = (page - 1) * limit
        return CrawlerRunListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun monitoring(): CrawlerMonitoringRecord {
        val allRuns = runs.values
        val successRuns = allRuns.filter { it.status.equals("SUCCESS", true) }
        return CrawlerMonitoringRecord(
            totalJobs = jobs.size,
            activeJobs = jobs.values.count { it.isActive },
            recentRunCount = allRuns.size,
            successCount = successRuns.size,
            failedCount = allRuns.count { it.status.equals("FAILED", true) },
            lastSuccessAt = successRuns.maxByOrNull { it.finishedAt ?: it.startedAt }?.finishedAt,
        )
    }

    override fun sellerExists(sellerId: Int): Boolean = sellerId in setOf(1, 2, 3)

    private fun sellerName(sellerId: Int): String =
        when (sellerId) {
            1 -> "PB 공식몰"
            2 -> "네스트 파트너스"
            3 -> "카월드"
            else -> "Seller $sellerId"
        }

    companion object {
        fun seeded(): InMemoryCrawlerRepository = InMemoryCrawlerRepository()
    }
}
