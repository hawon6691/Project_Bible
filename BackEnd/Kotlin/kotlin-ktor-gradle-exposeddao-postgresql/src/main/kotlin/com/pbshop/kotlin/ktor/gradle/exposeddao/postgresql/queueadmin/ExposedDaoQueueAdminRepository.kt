package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CrawlerLogStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CrawlerLogsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SearchIndexOutboxTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ShortformsTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoQueueAdminRepository(
    private val databaseFactory: DatabaseFactory,
) : QueueAdminRepository {
    override fun supportedQueues(): List<String> = SUPPORTED_QUEUES

    override fun getStats(): QueueStatsRecord =
        QueueStatsRecord(
            items = listOf(searchIndexStats(), crawlerStats(), videoTranscodeStats()),
            total = SUPPORTED_QUEUES.size,
        )

    override fun listFailedJobs(
        queueName: String,
        page: Int,
        limit: Int,
        newestFirst: Boolean,
    ): QueueJobListResult {
        requireQueue(queueName)
        val safePage = page.coerceAtLeast(1)
        val safeLimit = limit.coerceIn(1, 100)
        val offset = (safePage - 1) * safeLimit
        val items =
            when (queueName) {
                SEARCH_INDEX_QUEUE -> listFailedSearchIndex(newestFirst)
                CRAWLER_QUEUE -> listFailedCrawlerRuns(newestFirst)
                VIDEO_TRANSCODE_QUEUE -> listFailedVideoTranscodes(newestFirst)
                else -> error("Unsupported queue")
            }
        return QueueJobListResult(
            items = items.drop(offset).take(safeLimit),
            totalCount = items.size,
            page = safePage,
            limit = safeLimit,
        )
    }

    override fun retryFailedJobs(
        queueName: String,
        limit: Int,
    ): QueueRetryResultRecord {
        requireQueue(queueName)
        val safeLimit = limit.coerceAtLeast(1)
        return when (queueName) {
            SEARCH_INDEX_QUEUE -> retryFailedSearchIndex(safeLimit)
            CRAWLER_QUEUE -> retryFailedCrawlerRuns(safeLimit)
            VIDEO_TRANSCODE_QUEUE -> retryFailedVideoTranscodes(safeLimit)
            else -> error("Unsupported queue")
        }
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
            val candidateCount = listFailedJobs(queueName, 1, Int.MAX_VALUE, true).totalCount
            val allowed = minOf(safePerQueue, safeMaxTotal - retriedTotal)
            val retried = retryFailedJobs(queueName, allowed)
            retriedTotal += retried.requeuedCount
            items +=
                QueueAutoRetryItemRecord(
                    queueName = queueName,
                    candidateCount = candidateCount,
                    retriedCount = retried.requeuedCount,
                    jobIds = retried.jobIds,
                )
        }
        return QueueAutoRetryResultRecord(safePerQueue, safeMaxTotal, retriedTotal, items)
    }

    override fun retryJob(
        queueName: String,
        jobId: String,
    ): QueueJobActionRecord {
        requireQueue(queueName)
        when (queueName) {
            SEARCH_INDEX_QUEUE -> retrySingleSearchIndex(jobId.toIntOrNull() ?: throw NoSuchElementException("Job not found"))
            CRAWLER_QUEUE -> retrySingleCrawlerRun(jobId.toLongOrNull() ?: throw NoSuchElementException("Job not found"))
            VIDEO_TRANSCODE_QUEUE -> retrySingleVideoTranscode(jobId.toIntOrNull() ?: throw NoSuchElementException("Job not found"))
        }
        return QueueJobActionRecord(queueName = queueName, jobId = jobId, retried = true)
    }

    override fun removeJob(
        queueName: String,
        jobId: String,
    ): QueueJobActionRecord {
        requireQueue(queueName)
        when (queueName) {
            SEARCH_INDEX_QUEUE ->
                databaseFactory.withTransaction {
                    val deleted = SearchIndexOutboxTable.deleteWhere { SearchIndexOutboxTable.id eq (jobId.toIntOrNull() ?: -1) }
                    if (deleted == 0) throw NoSuchElementException("Job not found")
                }
            CRAWLER_QUEUE ->
                databaseFactory.withTransaction {
                    val deleted = CrawlerLogsTable.deleteWhere { CrawlerLogsTable.id eq (jobId.toLongOrNull() ?: -1L) }
                    if (deleted == 0) throw NoSuchElementException("Job not found")
                }
            VIDEO_TRANSCODE_QUEUE ->
                databaseFactory.withTransaction {
                    val id = jobId.toIntOrNull() ?: throw NoSuchElementException("Job not found")
                    val updated =
                        ShortformsTable.update({ ShortformsTable.id eq id }) {
                            it[transcodeStatus] = "REMOVED"
                            it[transcodeError] = null
                            it[updatedAt] = Instant.now()
                        }
                    if (updated == 0) throw NoSuchElementException("Job not found")
                }
        }
        return QueueJobActionRecord(queueName = queueName, jobId = jobId, removed = true)
    }

    private fun requireQueue(queueName: String) {
        require(queueName in SUPPORTED_QUEUES) { "지원하지 않는 큐입니다. (${SUPPORTED_QUEUES.joinToString(", ")})" }
    }

    private fun searchIndexStats(): QueueStatsItemRecord =
        databaseFactory.withTransaction {
            val rows = SearchIndexOutboxTable.selectAll().toList()
            QueueStatsItemRecord(
                queueName = SEARCH_INDEX_QUEUE,
                paused = false,
                counts =
                    QueueCountsRecord(
                        waiting = rows.count { it[SearchIndexOutboxTable.status] == "PENDING" },
                        active = rows.count { it[SearchIndexOutboxTable.status] == "PROCESSING" },
                        completed = rows.count { it[SearchIndexOutboxTable.status] == "COMPLETED" },
                        failed = rows.count { it[SearchIndexOutboxTable.status] == "FAILED" },
                    ),
            )
        }

    private fun crawlerStats(): QueueStatsItemRecord =
        databaseFactory.withTransaction {
            val rows = CrawlerLogsTable.selectAll().toList()
            QueueStatsItemRecord(
                queueName = CRAWLER_QUEUE,
                paused = false,
                counts =
                    QueueCountsRecord(
                        waiting = 0,
                        active = rows.count { it[CrawlerLogsTable.finishedAt] == null && it[CrawlerLogsTable.status] != CrawlerLogStatus.FAILED },
                        completed = rows.count { it[CrawlerLogsTable.status] == CrawlerLogStatus.SUCCESS || it[CrawlerLogsTable.status] == CrawlerLogStatus.PARTIAL },
                        failed = rows.count { it[CrawlerLogsTable.status] == CrawlerLogStatus.FAILED },
                    ),
            )
        }

    private fun videoTranscodeStats(): QueueStatsItemRecord =
        databaseFactory.withTransaction {
            val rows = ShortformsTable.selectAll().toList()
            QueueStatsItemRecord(
                queueName = VIDEO_TRANSCODE_QUEUE,
                paused = false,
                counts =
                    QueueCountsRecord(
                        waiting = rows.count { it[ShortformsTable.transcodeStatus].equals("PENDING", true) },
                        active = rows.count { it[ShortformsTable.transcodeStatus].equals("PROCESSING", true) },
                        completed = rows.count { it[ShortformsTable.transcodeStatus].equals("COMPLETED", true) },
                        failed = rows.count { isShortformFailed(it[ShortformsTable.transcodeStatus], it[ShortformsTable.transcodeError]) },
                    ),
            )
        }

    private fun listFailedSearchIndex(newestFirst: Boolean): List<QueueJobSnapshotRecord> =
        databaseFactory.withTransaction {
            SearchIndexOutboxTable.selectAll()
                .where { SearchIndexOutboxTable.status eq "FAILED" }
                .orderBy(SearchIndexOutboxTable.updatedAt to if (newestFirst) SortOrder.DESC else SortOrder.ASC)
                .map {
                    QueueJobSnapshotRecord(
                        id = it[SearchIndexOutboxTable.id].value.toString(),
                        name = it[SearchIndexOutboxTable.eventType],
                        data = mapOf("aggregateId" to it[SearchIndexOutboxTable.aggregateId]),
                        timestamp = it[SearchIndexOutboxTable.createdAt].toEpochMilli(),
                        processedOn = it[SearchIndexOutboxTable.processedAt]?.toEpochMilli(),
                        finishedOn = it[SearchIndexOutboxTable.updatedAt].toEpochMilli(),
                        attemptsMade = it[SearchIndexOutboxTable.attemptCount],
                        failedReason = it[SearchIndexOutboxTable.lastError],
                        stacktrace = emptyList(),
                    )
                }
        }

    private fun listFailedCrawlerRuns(newestFirst: Boolean): List<QueueJobSnapshotRecord> =
        databaseFactory.withTransaction {
            CrawlerLogsTable.selectAll()
                .where { CrawlerLogsTable.status eq CrawlerLogStatus.FAILED }
                .orderBy(CrawlerLogsTable.startedAt to if (newestFirst) SortOrder.DESC else SortOrder.ASC)
                .map {
                    QueueJobSnapshotRecord(
                        id = it[CrawlerLogsTable.id].value.toString(),
                        name = "crawler-run",
                        data = mapOf("jobId" to it[CrawlerLogsTable.job].value, "itemsFailed" to it[CrawlerLogsTable.itemsFailed]),
                        timestamp = it[CrawlerLogsTable.createdAt].toEpochMilli(),
                        processedOn = it[CrawlerLogsTable.startedAt].toEpochMilli(),
                        finishedOn = it[CrawlerLogsTable.finishedAt]?.toEpochMilli(),
                        attemptsMade = 1,
                        failedReason = it[CrawlerLogsTable.errorMessage],
                        stacktrace = emptyList(),
                    )
                }
        }

    private fun listFailedVideoTranscodes(newestFirst: Boolean): List<QueueJobSnapshotRecord> =
        databaseFactory.withTransaction {
            ShortformsTable.selectAll()
                .where {
                    (ShortformsTable.transcodeStatus eq "FAILED") or
                        (ShortformsTable.transcodeStatus eq "ERROR") or
                        ShortformsTable.transcodeError.isNotNull()
                }
                .orderBy(ShortformsTable.updatedAt to if (newestFirst) SortOrder.DESC else SortOrder.ASC)
                .map {
                    QueueJobSnapshotRecord(
                        id = it[ShortformsTable.id].value.toString(),
                        name = "transcode",
                        data = mapOf("shortformId" to it[ShortformsTable.id].value, "title" to it[ShortformsTable.title]),
                        timestamp = it[ShortformsTable.createdAt].toEpochMilli(),
                        processedOn = null,
                        finishedOn = it[ShortformsTable.transcodedAt]?.toEpochMilli(),
                        attemptsMade = 1,
                        failedReason = it[ShortformsTable.transcodeError] ?: it[ShortformsTable.transcodeStatus],
                        stacktrace = emptyList(),
                    )
                }
        }

    private fun retryFailedSearchIndex(limit: Int): QueueRetryResultRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val ids =
                SearchIndexOutboxTable.selectAll()
                    .where { SearchIndexOutboxTable.status eq "FAILED" }
                    .orderBy(SearchIndexOutboxTable.updatedAt to SortOrder.ASC, SearchIndexOutboxTable.id to SortOrder.ASC)
                    .limit(limit)
                    .map { it[SearchIndexOutboxTable.id].value }
            ids.forEach { id ->
                SearchIndexOutboxTable.update({ SearchIndexOutboxTable.id eq id }) {
                    it[status] = "PENDING"
                    it[lastError] = null
                    it[processedAt] = null
                    it[updatedAt] = now
                }
            }
            QueueRetryResultRecord(SEARCH_INDEX_QUEUE, ids.size, ids.size, ids.map(Int::toString))
        }

    private fun retryFailedCrawlerRuns(limit: Int): QueueRetryResultRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val ids =
                CrawlerLogsTable.selectAll()
                    .where { CrawlerLogsTable.status eq CrawlerLogStatus.FAILED }
                    .orderBy(CrawlerLogsTable.startedAt to SortOrder.DESC)
                    .limit(limit)
                    .map { it[CrawlerLogsTable.id].value }
            ids.forEach { id ->
                CrawlerLogsTable.update({ CrawlerLogsTable.id eq id }) {
                    it[status] = CrawlerLogStatus.SUCCESS
                    it[errorMessage] = null
                    it[finishedAt] = now
                    it[durationMs] = 0
                }
            }
            QueueRetryResultRecord(CRAWLER_QUEUE, ids.size, ids.size, ids.map(Long::toString))
        }

    private fun retryFailedVideoTranscodes(limit: Int): QueueRetryResultRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val ids =
                ShortformsTable.selectAll()
                    .where {
                        (ShortformsTable.transcodeStatus eq "FAILED") or
                            (ShortformsTable.transcodeStatus eq "ERROR") or
                            ShortformsTable.transcodeError.isNotNull()
                    }
                    .orderBy(ShortformsTable.updatedAt to SortOrder.DESC)
                    .limit(limit)
                    .map { it[ShortformsTable.id].value }
            ids.forEach { id ->
                ShortformsTable.update({ ShortformsTable.id eq id }) {
                    it[transcodeStatus] = "PENDING"
                    it[transcodeError] = null
                    it[transcodedAt] = null
                    it[updatedAt] = now
                }
            }
            QueueRetryResultRecord(VIDEO_TRANSCODE_QUEUE, ids.size, ids.size, ids.map(Int::toString))
        }

    private fun retrySingleSearchIndex(id: Int) {
        databaseFactory.withTransaction {
            val current = SearchIndexOutboxTable.selectAll().where { SearchIndexOutboxTable.id eq id }.singleOrNull() ?: throw NoSuchElementException("Job not found")
            if (current[SearchIndexOutboxTable.status] != "FAILED") {
                throw IllegalStateException("실패 상태 Job만 재시도할 수 있습니다. (current: ${current[SearchIndexOutboxTable.status]})")
            }
            SearchIndexOutboxTable.update({ SearchIndexOutboxTable.id eq id }) {
                it[status] = "PENDING"
                it[lastError] = null
                it[processedAt] = null
                it[updatedAt] = Instant.now()
            }
        }
    }

    private fun retrySingleCrawlerRun(id: Long) {
        databaseFactory.withTransaction {
            val current = CrawlerLogsTable.selectAll().where { CrawlerLogsTable.id eq id }.singleOrNull() ?: throw NoSuchElementException("Job not found")
            if (current[CrawlerLogsTable.status] != CrawlerLogStatus.FAILED) {
                throw IllegalStateException("실패 상태 Job만 재시도할 수 있습니다. (current: ${current[CrawlerLogsTable.status].name.lowercase()})")
            }
            CrawlerLogsTable.update({ CrawlerLogsTable.id eq id }) {
                it[status] = CrawlerLogStatus.SUCCESS
                it[errorMessage] = null
                it[finishedAt] = Instant.now()
                it[durationMs] = 0
            }
        }
    }

    private fun retrySingleVideoTranscode(id: Int) {
        databaseFactory.withTransaction {
            val current = ShortformsTable.selectAll().where { ShortformsTable.id eq id }.singleOrNull() ?: throw NoSuchElementException("Job not found")
            if (!isShortformFailed(current[ShortformsTable.transcodeStatus], current[ShortformsTable.transcodeError])) {
                throw IllegalStateException("실패 상태 Job만 재시도할 수 있습니다. (current: ${current[ShortformsTable.transcodeStatus]})")
            }
            ShortformsTable.update({ ShortformsTable.id eq id }) {
                it[transcodeStatus] = "PENDING"
                it[transcodeError] = null
                it[transcodedAt] = null
                it[updatedAt] = Instant.now()
            }
        }
    }

    private fun isShortformFailed(status: String, error: String?): Boolean =
        status.equals("FAILED", true) || status.equals("ERROR", true) || !error.isNullOrBlank()

    companion object {
        private const val SEARCH_INDEX_QUEUE = "search-index"
        private const val CRAWLER_QUEUE = "crawler"
        private const val VIDEO_TRANSCODE_QUEUE = "video-transcode"
        private val SUPPORTED_QUEUES = listOf(SEARCH_INDEX_QUEUE, CRAWLER_QUEUE, VIDEO_TRANSCODE_QUEUE)
    }
}
