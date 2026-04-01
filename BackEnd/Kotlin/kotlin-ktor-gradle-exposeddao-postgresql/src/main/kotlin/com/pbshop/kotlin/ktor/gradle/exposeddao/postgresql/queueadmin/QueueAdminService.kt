package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class QueueAdminService(
    private val repository: QueueAdminRepository,
) {
    fun supportedQueues(): StubResponse = StubResponse(data = mapOf("items" to repository.supportedQueues()))

    fun stats(): StubResponse = StubResponse(data = statsPayload(repository.getStats()))

    fun autoRetry(
        perQueueLimit: Int?,
        maxTotal: Int?,
    ): StubResponse {
        val safePerQueue = perQueueLimit ?: 20
        val safeMaxTotal = maxTotal ?: 100
        if (safePerQueue <= 0 || safeMaxTotal <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효한 재시도 제한 값이 필요합니다.")
        }
        val result = repository.autoRetryFailed(safePerQueue, safeMaxTotal)
        return StubResponse(
            data =
                mapOf(
                    "perQueueLimit" to result.perQueueLimit,
                    "maxTotal" to result.maxTotal,
                    "retriedTotal" to result.retriedTotal,
                    "items" to
                        result.items.map {
                            mapOf(
                                "queueName" to it.queueName,
                                "candidateCount" to it.candidateCount,
                                "retriedCount" to it.retriedCount,
                                "jobIds" to it.jobIds,
                            )
                        },
                ),
        )
    }

    fun failedJobs(
        queueName: String,
        page: Int,
        limit: Int,
        newestFirst: Boolean,
    ): StubResponse {
        validateQueue(queueName)
        val result = repository.listFailedJobs(queueName, page, limit, newestFirst)
        return StubResponse(
            data = result.items.map(::jobPayload),
            meta =
                mapOf(
                    "page" to result.page,
                    "limit" to result.limit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount - 1) / result.limit) + 1,
                ),
        )
    }

    fun retryFailedJobs(
        queueName: String,
        limit: Int?,
    ): StubResponse {
        validateQueue(queueName)
        val safeLimit = limit ?: 50
        if (safeLimit <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "limit은 1 이상이어야 합니다.")
        }
        val result = repository.retryFailedJobs(queueName, safeLimit)
        return StubResponse(
            data =
                mapOf(
                    "queueName" to result.queueName,
                    "requested" to result.requested,
                    "requeuedCount" to result.requeuedCount,
                    "jobIds" to result.jobIds,
                ),
        )
    }

    fun retryJob(
        queueName: String,
        jobId: String,
    ): StubResponse {
        validateQueue(queueName)
        return try {
            val result = repository.retryJob(queueName, jobId)
            StubResponse(data = mapOf("queueName" to result.queueName, "jobId" to result.jobId, "retried" to true))
        } catch (cause: NoSuchElementException) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "해당 Job을 찾을 수 없습니다.")
        } catch (cause: IllegalStateException) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", cause.message ?: "재시도할 수 없는 상태입니다.")
        }
    }

    fun removeJob(
        queueName: String,
        jobId: String,
    ): StubResponse {
        validateQueue(queueName)
        return try {
            val result = repository.removeJob(queueName, jobId)
            StubResponse(data = mapOf("queueName" to result.queueName, "jobId" to result.jobId, "removed" to true))
        } catch (cause: NoSuchElementException) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "해당 Job을 찾을 수 없습니다.")
        }
    }

    private fun validateQueue(queueName: String) {
        if (queueName !in repository.supportedQueues()) {
            throw PbShopException(
                HttpStatusCode.BadRequest,
                "VALIDATION_ERROR",
                "지원하지 않는 큐입니다. (${repository.supportedQueues().joinToString(", ")})",
            )
        }
    }

    private fun statsPayload(record: QueueStatsRecord): Map<String, Any?> =
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

    private fun jobPayload(record: QueueJobSnapshotRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "name" to record.name,
            "data" to record.data,
            "timestamp" to record.timestamp,
            "processedOn" to record.processedOn,
            "finishedOn" to record.finishedOn,
            "attemptsMade" to record.attemptsMade,
            "failedReason" to record.failedReason,
            "stacktrace" to record.stacktrace,
        )
}
