package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode
import java.util.UUID

class PcBuilderService(
    private val repository: PcBuilderRepository,
) {
    fun myBuilds(userId: Int, page: Int, limit: Int): StubResponse {
        ensureUserExists(userId)
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listMyBuilds(userId, normalizedPage, normalizedLimit)
        return StubResponse(data = result.items.map(::summaryPayload), meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount))
    }

    fun create(userId: Int, request: CreatePcBuildRequest): StubResponse {
        ensureUserExists(userId)
        validateCreateRequest(request)
        val created =
            repository.createBuild(
                userId,
                NewPcBuild(
                    name = request.name.trim(),
                    description = request.description?.trim()?.takeIf { it.isNotBlank() },
                    purpose = parsePurpose(request.purpose),
                    budget = request.budget?.takeIf { it > 0 },
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = detailPayload(created, computeCompatibility(created.parts)))
    }

    fun detail(buildId: Int): StubResponse {
        val build = requireBuild(buildId)
        return StubResponse(data = detailPayload(build, computeCompatibility(build.parts)))
    }

    fun update(userId: Int, buildId: Int, request: UpdatePcBuildRequest): StubResponse {
        requireOwnedBuild(userId, buildId)
        request.purpose?.let(::parsePurpose)
        request.budget?.let {
            if (it <= 0) {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "budget은 0보다 커야 합니다.")
            }
        }
        val updated =
            repository.updateBuild(
                buildId,
                PcBuildUpdate(
                    name = request.name?.trim(),
                    description = request.description?.trim(),
                    purpose = request.purpose?.trim()?.uppercase(),
                    budget = request.budget,
                ),
            )
        return StubResponse(data = detailPayload(updated, computeCompatibility(updated.parts)))
    }

    fun delete(userId: Int, buildId: Int): StubResponse {
        requireOwnedBuild(userId, buildId)
        repository.deleteBuild(buildId)
        return StubResponse(data = mapOf("message" to "견적이 삭제되었습니다."))
    }

    fun addPart(userId: Int, buildId: Int, request: AddPcBuildPartRequest): StubResponse {
        requireOwnedBuild(userId, buildId)
        validatePartRequest(request)
        if (!repository.productExists(request.productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
        if (repository.findPriceCandidate(request.productId, request.sellerId) == null) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "판매처 가격 정보를 찾을 수 없습니다.")
        }
        val updated =
            repository.upsertBuildPart(
                buildId,
                NewPcBuildPart(
                    productId = request.productId,
                    sellerId = request.sellerId,
                    partType = request.partType.trim().uppercase(),
                    quantity = request.quantity,
                ),
            )
        return StubResponse(data = detailPayload(updated, computeCompatibility(updated.parts)))
    }

    fun removePart(userId: Int, buildId: Int, partId: Int): StubResponse {
        requireOwnedBuild(userId, buildId)
        val updated =
            repository.removeBuildPart(buildId, partId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "부품을 찾을 수 없습니다.")
        return StubResponse(data = detailPayload(updated, computeCompatibility(updated.parts)))
    }

    fun compatibility(buildId: Int): StubResponse {
        val build = requireBuild(buildId)
        return StubResponse(data = compatibilityPayload(computeCompatibility(build.parts)))
    }

    fun share(userId: Int, buildId: Int): StubResponse {
        val owned = requireOwnedBuild(userId, buildId)
        val shareCode = owned.shareCode ?: UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
        val updated = if (owned.shareCode == null) repository.assignShareCode(buildId, shareCode) else owned
        return StubResponse(data = mapOf("shareUrl" to "https://pbshop.dev/pc-builds/shared/${updated.shareCode}", "shareCode" to updated.shareCode))
    }

    fun sharedBuild(shareCode: String): StubResponse {
        val build = repository.findBuildByShareCode(shareCode.trim())
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "공유 견적을 찾을 수 없습니다.")
        repository.incrementViewCount(build.id)
        val viewed = requireBuild(build.id)
        return StubResponse(data = detailPayload(viewed, computeCompatibility(viewed.parts)))
    }

    fun popular(page: Int, limit: Int): StubResponse {
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listPopularBuilds(normalizedPage, normalizedLimit)
        return StubResponse(data = result.items.map(::summaryPayload), meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount))
    }

    fun listRules(): StubResponse = StubResponse(data = repository.listCompatibilityRules().map(::rulePayload))

    fun createRule(request: CreateCompatibilityRuleRequest): StubResponse {
        validateRuleRequest(request.partType, request.targetPartType, request.title, request.description, request.severity)
        val created =
            repository.createCompatibilityRule(
                NewCompatibilityRule(
                    partType = request.partType.trim().uppercase(),
                    targetPartType = request.targetPartType?.trim()?.uppercase(),
                    title = request.title.trim(),
                    description = request.description.trim(),
                    severity = parseSeverity(request.severity),
                    enabled = request.enabled,
                    metadata = request.metadata?.mapValues { it.value.trim() }?.takeIf { it.isNotEmpty() },
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = rulePayload(created))
    }

    fun updateRule(ruleId: Int, request: UpdateCompatibilityRuleRequest): StubResponse {
        requireRule(ruleId)
        request.partType?.let(::parsePartType)
        request.targetPartType?.let(::parsePartType)
        request.severity?.let(::parseSeverity)
        val updated =
            repository.updateCompatibilityRule(
                ruleId,
                CompatibilityRuleUpdate(
                    partType = request.partType?.trim()?.uppercase(),
                    targetPartType = request.targetPartType?.trim()?.uppercase(),
                    title = request.title?.trim(),
                    description = request.description?.trim(),
                    severity = request.severity?.trim()?.uppercase(),
                    enabled = request.enabled,
                    metadata = request.metadata?.mapValues { it.value.trim() },
                ),
            )
        return StubResponse(data = rulePayload(updated))
    }

    fun deleteRule(ruleId: Int): StubResponse {
        if (!repository.deleteCompatibilityRule(ruleId)) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "호환성 규칙을 찾을 수 없습니다.")
        }
        return StubResponse(data = mapOf("message" to "호환성 규칙이 삭제되었습니다."))
    }

    private fun requireBuild(buildId: Int): PcBuildDetailRecord =
        repository.findBuildById(buildId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "견적을 찾을 수 없습니다.")

    private fun requireOwnedBuild(userId: Int, buildId: Int): PcBuildDetailRecord =
        repository.findOwnedBuild(userId, buildId)
            ?: throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "해당 견적에 대한 권한이 없습니다.")

    private fun requireRule(ruleId: Int): CompatibilityRuleRecord =
        repository.findCompatibilityRule(ruleId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "호환성 규칙을 찾을 수 없습니다.")

    private fun ensureUserExists(userId: Int) {
        if (!repository.userExists(userId)) {
            throw PbShopException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        }
    }

    private fun validateCreateRequest(request: CreatePcBuildRequest) {
        if (request.name.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "견적명은 필수입니다.")
        }
        parsePurpose(request.purpose)
        request.budget?.let {
            if (it <= 0) {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "budget은 0보다 커야 합니다.")
            }
        }
    }

    private fun validatePartRequest(request: AddPcBuildPartRequest) {
        parsePartType(request.partType)
        if (request.quantity <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "quantity는 1 이상이어야 합니다.")
        }
    }

    private fun validateRuleRequest(partType: String, targetPartType: String?, title: String, description: String, severity: String) {
        parsePartType(partType)
        targetPartType?.let(::parsePartType)
        parseSeverity(severity)
        if (title.trim().isBlank() || description.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "호환성 규칙 값이 올바르지 않습니다.")
        }
    }

    private fun parsePurpose(value: String): String =
        value.trim().uppercase().takeIf { it in validPurposes }
            ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 purpose입니다.")

    private fun parsePartType(value: String): String =
        value.trim().uppercase().takeIf { it in validPartTypes }
            ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 partType입니다.")

    private fun parseSeverity(value: String): String =
        value.trim().uppercase().takeIf { it in validSeverities }
            ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 severity입니다.")

    private fun summaryPayload(record: PcBuildSummaryRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "userId" to record.userId,
            "name" to record.name,
            "description" to record.description,
            "purpose" to record.purpose,
            "budget" to record.budget,
            "totalPrice" to record.totalPrice,
            "shareCode" to record.shareCode,
            "viewCount" to record.viewCount,
            "partCount" to record.parts.size,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun detailPayload(record: PcBuildDetailRecord, compatibility: CompatibilityComputation): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "userId" to record.userId,
            "name" to record.name,
            "description" to record.description,
            "purpose" to record.purpose,
            "budget" to record.budget,
            "parts" to record.parts.map(::partPayload),
            "totalPrice" to record.totalPrice,
            "compatibility" to compatibilityPayload(compatibility),
            "shareCode" to record.shareCode,
            "viewCount" to record.viewCount,
            "bottleneck" to null,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun partPayload(record: PcBuildPartRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "buildId" to record.buildId,
            "productId" to record.productId,
            "sellerId" to record.sellerId,
            "partType" to record.partType,
            "quantity" to record.quantity,
            "unitPrice" to record.unitPrice,
            "totalPrice" to record.totalPrice,
            "product" to mapOf("id" to record.product.id, "name" to record.product.name, "thumbnailUrl" to record.product.thumbnailUrl, "lowestPrice" to record.product.lowestPrice),
            "seller" to record.seller?.let { mapOf("id" to it.id, "name" to it.name, "price" to it.price) },
        )

    private fun compatibilityPayload(result: CompatibilityComputation): Map<String, Any?> =
        mapOf(
            "status" to result.status,
            "issues" to result.issues.map(::issuePayload),
            "warnings" to result.warnings.map(::issuePayload),
            "errors" to result.issues.map(::issuePayload),
            "missingParts" to result.missingParts,
            "powerEstimate" to mapOf(
                "totalWattage" to result.powerEstimate.totalWattage,
                "psuWattage" to result.powerEstimate.psuWattage,
                "headroom" to result.powerEstimate.headroom,
                "sufficient" to result.powerEstimate.sufficient,
            ),
            "socketCompatible" to result.issues.none { it.type == "SOCKET" },
            "ramCompatible" to result.issues.none { it.type == "RAM" },
            "formFactorCompatible" to result.issues.none { it.type == "FORM_FACTOR" },
        )

    private fun issuePayload(record: CompatibilityIssueRecord): Map<String, Any?> =
        mapOf("type" to record.type, "title" to record.title, "message" to record.message, "severity" to record.severity, "ruleId" to record.ruleId)

    private fun rulePayload(record: CompatibilityRuleRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "partType" to record.partType,
            "targetPartType" to record.targetPartType,
            "title" to record.title,
            "description" to record.description,
            "severity" to record.severity,
            "enabled" to record.enabled,
            "metadata" to record.metadata,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun pageMeta(page: Int, limit: Int, totalCount: Int): Map<String, Int> =
        mapOf("page" to page, "limit" to limit, "totalCount" to totalCount, "totalPages" to if (totalCount == 0) 0 else ((totalCount - 1) / limit) + 1)

    private fun normalizePage(page: Int): Int = if (page > 0) page else 1

    private fun normalizeLimit(limit: Int): Int = limit.coerceIn(1, 100).takeIf { it > 0 } ?: 20

    private fun computeCompatibility(parts: List<PcBuildPartRecord>): CompatibilityComputation {
        if (parts.isEmpty()) {
            return CompatibilityComputation("EMPTY", emptyList(), emptyList(), requiredPartTypes.toList(), PowerEstimate(0, null, null, true))
        }
        val selectedTypes = parts.map { it.partType }.toSet()
        val issues = mutableListOf<CompatibilityIssueRecord>()
        val warnings = mutableListOf<CompatibilityIssueRecord>()
        repository.listCompatibilityRules().filter { it.enabled }.forEach { rule ->
            val hasSource = selectedTypes.contains(rule.partType)
            val hasTarget = rule.targetPartType?.let(selectedTypes::contains) ?: true
            if (hasSource && !hasTarget) {
                val issue = CompatibilityIssueRecord(type = rule.partType, title = rule.title, message = rule.description, severity = rule.severity, ruleId = rule.id)
                if (rule.severity in setOf("HIGH", "CRITICAL")) issues += issue else warnings += issue
            }
        }
        val missingParts = requiredPartTypes.filterNot(selectedTypes::contains)
        missingParts.forEach {
            warnings += CompatibilityIssueRecord(type = "MISSING_PART", title = "필수 부품 누락", message = "$it 부품을 선택해주세요.", severity = "LOW")
        }
        val totalWattage = parts.sumOf { wattageByPartType[it.partType] ?: 50 }
        val psuPart = parts.firstOrNull { it.partType == "PSU" }
        val psuWattage = psuPart?.let { (it.unitPrice / 200).coerceAtLeast(500) }
        val headroom = psuWattage?.minus(totalWattage)
        if (psuWattage != null && headroom != null && headroom < 100) {
            warnings += CompatibilityIssueRecord(type = "POWER", title = "전력 여유 부족", message = "현재 PSU 용량의 여유가 충분하지 않습니다.", severity = "MEDIUM")
        }
        val status =
            when {
                issues.isNotEmpty() -> "ERROR"
                missingParts.isNotEmpty() -> "INCOMPLETE"
                warnings.isNotEmpty() -> "WARNING"
                else -> "OK"
            }
        return CompatibilityComputation(status, issues, warnings, missingParts, PowerEstimate(totalWattage, psuWattage, headroom, headroom == null || headroom >= 100))
    }

    private data class CompatibilityComputation(
        val status: String,
        val issues: List<CompatibilityIssueRecord>,
        val warnings: List<CompatibilityIssueRecord>,
        val missingParts: List<String>,
        val powerEstimate: PowerEstimate,
    )

    private data class PowerEstimate(val totalWattage: Int, val psuWattage: Int?, val headroom: Int?, val sufficient: Boolean)

    companion object {
        private val validPurposes = setOf("GAMING", "OFFICE", "DESIGN", "DEVELOPMENT", "STREAMING")
        private val validPartTypes = setOf("CPU", "MOTHERBOARD", "RAM", "GPU", "SSD", "HDD", "PSU", "CASE", "COOLER", "MONITOR")
        private val validSeverities = setOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
        private val requiredPartTypes = setOf("CPU", "MOTHERBOARD", "RAM", "GPU", "SSD", "PSU", "CASE")
        private val wattageByPartType = mapOf("CPU" to 120, "MOTHERBOARD" to 70, "RAM" to 20, "GPU" to 250, "SSD" to 10, "HDD" to 15, "PSU" to 0, "CASE" to 10, "COOLER" to 10, "MONITOR" to 35)
    }
}
