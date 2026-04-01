package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin

data class QueueCountsRecord(
    val waiting: Int,
    val active: Int,
    val completed: Int,
    val failed: Int,
)

data class QueueStatsItemRecord(
    val queueName: String,
    val paused: Boolean,
    val counts: QueueCountsRecord,
)

data class QueueStatsRecord(
    val items: List<QueueStatsItemRecord>,
    val total: Int,
)

data class QueueJobSnapshotRecord(
    val id: String,
    val name: String,
    val data: Map<String, Any?>,
    val timestamp: Long,
    val processedOn: Long?,
    val finishedOn: Long?,
    val attemptsMade: Int,
    val failedReason: String?,
    val stacktrace: List<String>,
)

data class QueueJobListResult(
    val items: List<QueueJobSnapshotRecord>,
    val totalCount: Int,
    val page: Int,
    val limit: Int,
)

data class QueueRetryResultRecord(
    val queueName: String,
    val requested: Int,
    val requeuedCount: Int,
    val jobIds: List<String>,
)

data class QueueAutoRetryItemRecord(
    val queueName: String,
    val candidateCount: Int,
    val retriedCount: Int,
    val jobIds: List<String>,
)

data class QueueAutoRetryResultRecord(
    val perQueueLimit: Int,
    val maxTotal: Int,
    val retriedTotal: Int,
    val items: List<QueueAutoRetryItemRecord>,
)

data class QueueJobActionRecord(
    val queueName: String,
    val jobId: String,
    val retried: Boolean? = null,
    val removed: Boolean? = null,
)

interface QueueAdminRepository {
    fun supportedQueues(): List<String>

    fun getStats(): QueueStatsRecord

    fun listFailedJobs(
        queueName: String,
        page: Int,
        limit: Int,
        newestFirst: Boolean,
    ): QueueJobListResult

    fun retryFailedJobs(
        queueName: String,
        limit: Int,
    ): QueueRetryResultRecord

    fun autoRetryFailed(
        perQueueLimit: Int,
        maxTotal: Int,
    ): QueueAutoRetryResultRecord

    fun retryJob(
        queueName: String,
        jobId: String,
    ): QueueJobActionRecord

    fun removeJob(
        queueName: String,
        jobId: String,
    ): QueueJobActionRecord
}
