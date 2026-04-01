package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin

import java.time.Instant

private data class InMemoryQueueJob(
    val queueName: String,
    val id: String,
    val name: String,
    val data: Map<String, Any?>,
    val timestamp: Long,
    var status: String,
    var processedOn: Long?,
    var finishedOn: Long?,
    var attemptsMade: Int,
    var failedReason: String?,
    val stacktrace: List<String>,
)

class InMemoryQueueAdminRepository private constructor(
    private val jobsByQueue: MutableMap<String, MutableList<InMemoryQueueJob>>,
) : QueueAdminRepository {
    override fun supportedQueues(): List<String> = SUPPORTED_QUEUES

    override fun getStats(): QueueStatsRecord =
        QueueStatsRecord(
            items =
                SUPPORTED_QUEUES.map { queueName ->
                    val jobs = jobsByQueue[queueName].orEmpty()
                    QueueStatsItemRecord(
                        queueName = queueName,
                        paused = false,
                        counts =
                            QueueCountsRecord(
                                waiting = jobs.count { it.status == STATUS_WAITING },
                                active = jobs.count { it.status == STATUS_ACTIVE },
                                completed = jobs.count { it.status == STATUS_COMPLETED },
                                failed = jobs.count { it.status == STATUS_FAILED },
                            ),
                    )
                },
            total = SUPPORTED_QUEUES.size,
        )

    override fun listFailedJobs(
        queueName: String,
        page: Int,
        limit: Int,
        newestFirst: Boolean,
    ): QueueJobListResult {
        requireQueue(queueName)
        val filtered =
            jobsByQueue[queueName]
                .orEmpty()
                .filter { it.status == STATUS_FAILED }
                .sortedBy { it.timestamp }
                .let { if (newestFirst) it.reversed() else it }
        val safePage = page.coerceAtLeast(1)
        val safeLimit = limit.coerceIn(1, 100)
        val offset = (safePage - 1) * safeLimit
        return QueueJobListResult(
            items = filtered.drop(offset).take(safeLimit).map(::toSnapshot),
            totalCount = filtered.size,
            page = safePage,
            limit = safeLimit,
        )
    }

    override fun retryFailedJobs(
        queueName: String,
        limit: Int,
    ): QueueRetryResultRecord {
        requireQueue(queueName)
        val targets =
            jobsByQueue[queueName]
                .orEmpty()
                .filter { it.status == STATUS_FAILED }
                .sortedByDescending { it.timestamp }
                .take(limit.coerceAtLeast(1))
        targets.forEach {
            it.status = if (queueName == VIDEO_TRANSCODE_QUEUE) STATUS_WAITING else STATUS_COMPLETED
            it.failedReason = null
            it.processedOn = Instant.now().toEpochMilli()
            it.finishedOn = Instant.now().toEpochMilli()
            it.attemptsMade += 1
        }
        return QueueRetryResultRecord(
            queueName = queueName,
            requested = targets.size,
            requeuedCount = targets.size,
            jobIds = targets.map { it.id },
        )
    }

    override fun autoRetryFailed(
        perQueueLimit: Int,
        maxTotal: Int,
    ): QueueAutoRetryResultRecord {
        val safePerQueue = perQueueLimit.coerceAtLeast(1)
        val safeMaxTotal = maxTotal.coerceAtLeast(1)
        var retriedTotal = 0
        val items = mutableListOf<QueueAutoRetryItemRecord>()
        supportedQueues().forEach { queueName ->
            if (retriedTotal >= safeMaxTotal) {
                return@forEach
            }
            val candidates =
                jobsByQueue[queueName]
                    .orEmpty()
                    .count { it.status == STATUS_FAILED }
            val allowed = minOf(safePerQueue, safeMaxTotal - retriedTotal)
            val retried = retryFailedJobs(queueName, allowed)
            retriedTotal += retried.requeuedCount
            items +=
                QueueAutoRetryItemRecord(
                    queueName = queueName,
                    candidateCount = candidates,
                    retriedCount = retried.requeuedCount,
                    jobIds = retried.jobIds,
                )
        }
        return QueueAutoRetryResultRecord(
            perQueueLimit = safePerQueue,
            maxTotal = safeMaxTotal,
            retriedTotal = retriedTotal,
            items = items,
        )
    }

    override fun retryJob(
        queueName: String,
        jobId: String,
    ): QueueJobActionRecord {
        requireQueue(queueName)
        val job = jobsByQueue[queueName].orEmpty().firstOrNull { it.id == jobId } ?: throw NoSuchElementException("Job not found")
        if (job.status != STATUS_FAILED) {
            throw IllegalStateException("실패 상태 Job만 재시도할 수 있습니다. (current: ${job.status})")
        }
        retryFailedJobs(queueName, 1)
        return QueueJobActionRecord(queueName = queueName, jobId = jobId, retried = true)
    }

    override fun removeJob(
        queueName: String,
        jobId: String,
    ): QueueJobActionRecord {
        requireQueue(queueName)
        val removed = jobsByQueue[queueName]?.removeIf { it.id == jobId } == true
        if (!removed) {
            throw NoSuchElementException("Job not found")
        }
        return QueueJobActionRecord(queueName = queueName, jobId = jobId, removed = true)
    }

    private fun requireQueue(queueName: String) {
        require(queueName in SUPPORTED_QUEUES) { "지원하지 않는 큐입니다. (${SUPPORTED_QUEUES.joinToString(", ")})" }
    }

    private fun toSnapshot(job: InMemoryQueueJob): QueueJobSnapshotRecord =
        QueueJobSnapshotRecord(
            id = job.id,
            name = job.name,
            data = job.data,
            timestamp = job.timestamp,
            processedOn = job.processedOn,
            finishedOn = job.finishedOn,
            attemptsMade = job.attemptsMade,
            failedReason = job.failedReason,
            stacktrace = job.stacktrace,
        )

    companion object {
        private const val SEARCH_INDEX_QUEUE = "search-index"
        private const val CRAWLER_QUEUE = "crawler"
        private const val VIDEO_TRANSCODE_QUEUE = "video-transcode"
        private const val STATUS_WAITING = "waiting"
        private const val STATUS_ACTIVE = "active"
        private const val STATUS_COMPLETED = "completed"
        private const val STATUS_FAILED = "failed"
        private val SUPPORTED_QUEUES = listOf(SEARCH_INDEX_QUEUE, CRAWLER_QUEUE, VIDEO_TRANSCODE_QUEUE)

        fun seeded(): InMemoryQueueAdminRepository {
            val now = Instant.now().toEpochMilli()
            return InMemoryQueueAdminRepository(
                mutableMapOf(
                    SEARCH_INDEX_QUEUE to
                        mutableListOf(
                            InMemoryQueueJob(
                                queueName = SEARCH_INDEX_QUEUE,
                                id = "1",
                                name = "PRODUCT_REINDEX",
                                data = mapOf("aggregateId" to 1),
                                timestamp = now - 3_600_000,
                                status = STATUS_FAILED,
                                processedOn = null,
                                finishedOn = now - 3_590_000,
                                attemptsMade = 2,
                                failedReason = "index writer timeout",
                                stacktrace = listOf("SearchWriterTimeoutException"),
                            ),
                        ),
                    CRAWLER_QUEUE to
                        mutableListOf(
                            InMemoryQueueJob(
                                queueName = CRAWLER_QUEUE,
                                id = "11",
                                name = "crawler-run",
                                data = mapOf("jobId" to 1),
                                timestamp = now - 7_200_000,
                                status = STATUS_FAILED,
                                processedOn = now - 7_195_000,
                                finishedOn = now - 7_190_000,
                                attemptsMade = 1,
                                failedReason = "seller endpoint timeout",
                                stacktrace = listOf("CrawlerTimeoutException"),
                            ),
                        ),
                    VIDEO_TRANSCODE_QUEUE to
                        mutableListOf(
                            InMemoryQueueJob(
                                queueName = VIDEO_TRANSCODE_QUEUE,
                                id = "21",
                                name = "transcode",
                                data = mapOf("shortformId" to 1, "title" to "게이밍 조립 팁"),
                                timestamp = now - 1_800_000,
                                status = STATUS_FAILED,
                                processedOn = now - 1_790_000,
                                finishedOn = now - 1_780_000,
                                attemptsMade = 1,
                                failedReason = "ffmpeg crashed",
                                stacktrace = listOf("FFmpegException"),
                            ),
                        ),
                ),
            )
        }
    }
}
