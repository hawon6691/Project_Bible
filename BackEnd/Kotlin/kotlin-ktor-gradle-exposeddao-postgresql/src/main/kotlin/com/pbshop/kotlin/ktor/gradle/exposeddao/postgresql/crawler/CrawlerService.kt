package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class CrawlerService(
    private val repository: CrawlerRepository,
) {
    fun jobs(
        status: String?,
        page: Int?,
        limit: Int?,
    ): StubResponse {
        val queryPage = (page ?: 1).coerceAtLeast(1)
        val queryLimit = (limit ?: 20).coerceIn(1, 100)
        val result = repository.listJobs(status?.trim(), queryPage, queryLimit)
        return StubResponse(
            data = result.items.map(::jobPayload),
            meta = pageMeta(queryPage, queryLimit, result.totalCount),
        )
    }

    fun create(request: CrawlerJobCreateRequest): StubResponse {
        validate(request.name, request.sellerId)
        val created =
            repository.createJob(
                NewCrawlerJob(
                    sellerId = request.sellerId,
                    name = request.name.trim(),
                    cronExpression = request.cronExpression?.trim(),
                    collectPrice = request.collectPrice,
                    collectSpec = request.collectSpec,
                    detectAnomaly = request.detectAnomaly,
                    isActive = request.isActive,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = jobPayload(created))
    }

    fun update(
        id: Int,
        request: CrawlerJobUpdateRequest,
    ): StubResponse {
        request.name?.let { validate(it, request.sellerId ?: repository.findJobById(id)?.sellerId ?: 0) }
        request.sellerId?.let { ensureSellerExists(it) }
        val updated =
            repository.updateJob(
                id,
                CrawlerJobUpdate(
                    sellerId = request.sellerId,
                    name = request.name?.trim(),
                    cronExpression = request.cronExpression?.trim(),
                    collectPrice = request.collectPrice,
                    collectSpec = request.collectSpec,
                    detectAnomaly = request.detectAnomaly,
                    isActive = request.isActive,
                ),
            )
        return StubResponse(data = jobPayload(updated))
    }

    fun delete(id: Int): StubResponse {
        repository.deleteJob(id)
        return StubResponse(data = mapOf("message" to "Crawler job deleted."))
    }

    fun run(id: Int): StubResponse =
        repository.triggerJob(id).let { StubResponse(data = mapOf("message" to it.first, "runId" to it.second)) }

    fun trigger(request: CrawlerTriggerRequest): StubResponse =
        repository.triggerManual(request.jobId, request.sellerId, request.name?.trim())
            .let { StubResponse(data = mapOf("message" to it.first, "runId" to it.second)) }

    fun runs(
        status: String?,
        jobId: Int?,
        page: Int?,
        limit: Int?,
    ): StubResponse {
        val queryPage = (page ?: 1).coerceAtLeast(1)
        val queryLimit = (limit ?: 20).coerceIn(1, 100)
        val result = repository.listRuns(status?.trim(), jobId, queryPage, queryLimit)
        return StubResponse(
            data =
                result.items.map {
                    mapOf(
                        "id" to it.id,
                        "jobId" to it.jobId,
                        "jobName" to it.jobName,
                        "status" to it.status,
                        "startedAt" to it.startedAt.toString(),
                        "finishedAt" to it.finishedAt?.toString(),
                        "durationMs" to it.durationMs,
                        "itemsProcessed" to it.itemsProcessed,
                        "itemsCreated" to it.itemsCreated,
                        "itemsUpdated" to it.itemsUpdated,
                        "itemsFailed" to it.itemsFailed,
                        "errorMessage" to it.errorMessage,
                    )
                },
            meta = pageMeta(queryPage, queryLimit, result.totalCount),
        )
    }

    fun monitoring(): StubResponse =
        repository.monitoring().let {
            StubResponse(
                data =
                    mapOf(
                        "totalJobs" to it.totalJobs,
                        "activeJobs" to it.activeJobs,
                        "recentRunCount" to it.recentRunCount,
                        "successCount" to it.successCount,
                        "failedCount" to it.failedCount,
                        "lastSuccessAt" to it.lastSuccessAt?.toString(),
                    ),
            )
        }

    private fun validate(
        name: String,
        sellerId: Int,
    ) {
        if (name.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "크롤러 작업명은 비어 있을 수 없습니다.")
        }
        ensureSellerExists(sellerId)
    }

    private fun ensureSellerExists(sellerId: Int) {
        if (!repository.sellerExists(sellerId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "SELLER_NOT_FOUND", "판매처를 찾을 수 없습니다.")
        }
    }

    private fun jobPayload(record: CrawlerJobRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "sellerId" to record.sellerId,
            "sellerName" to record.sellerName,
            "name" to record.name,
            "cronExpression" to record.cronExpression,
            "collectPrice" to record.collectPrice,
            "collectSpec" to record.collectSpec,
            "detectAnomaly" to record.detectAnomaly,
            "isActive" to record.isActive,
            "lastTriggeredAt" to record.lastTriggeredAt?.toString(),
            "status" to record.latestStatus,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun pageMeta(
        page: Int,
        limit: Int,
        totalCount: Int,
    ): Map<String, Int> =
        mapOf(
            "page" to page,
            "limit" to limit,
            "totalCount" to totalCount,
            "totalPages" to if (totalCount == 0) 0 else ((totalCount + limit - 1) / limit),
        )
}
