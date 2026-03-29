package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder

import java.time.Instant

data class PcBuildProductRecord(
    val id: Int,
    val name: String,
    val thumbnailUrl: String?,
    val lowestPrice: Int,
)

data class PcBuildSellerRecord(
    val id: Int,
    val name: String,
    val price: Int,
)

data class PcBuildPartRecord(
    val id: Int,
    val buildId: Int,
    val productId: Int,
    val sellerId: Int,
    val partType: String,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int,
    val product: PcBuildProductRecord,
    val seller: PcBuildSellerRecord?,
)

data class PcBuildSummaryRecord(
    val id: Int,
    val userId: Int,
    val name: String,
    val description: String?,
    val purpose: String,
    val budget: Int?,
    val totalPrice: Int,
    val shareCode: String?,
    val viewCount: Int,
    val parts: List<PcBuildPartRecord>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class PcBuildDetailRecord(
    val id: Int,
    val userId: Int,
    val name: String,
    val description: String?,
    val purpose: String,
    val budget: Int?,
    val totalPrice: Int,
    val shareCode: String?,
    val viewCount: Int,
    val parts: List<PcBuildPartRecord>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CompatibilityIssueRecord(
    val type: String,
    val message: String,
    val severity: String,
    val ruleId: Int? = null,
    val title: String? = null,
)

data class CompatibilityResultRecord(
    val status: String,
    val issues: List<CompatibilityIssueRecord>,
    val warnings: List<CompatibilityIssueRecord>,
)

data class CompatibilityRuleRecord(
    val id: Int,
    val partType: String,
    val targetPartType: String?,
    val title: String,
    val description: String,
    val severity: String,
    val enabled: Boolean,
    val metadata: Map<String, String>?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class PcBuildListResult(
    val items: List<PcBuildSummaryRecord>,
    val totalCount: Int,
)

data class NewPcBuild(
    val name: String,
    val description: String?,
    val purpose: String,
    val budget: Int?,
)

data class PcBuildUpdate(
    val name: String? = null,
    val description: String? = null,
    val purpose: String? = null,
    val budget: Int? = null,
)

data class NewPcBuildPart(
    val productId: Int,
    val sellerId: Int?,
    val partType: String,
    val quantity: Int,
)

data class NewCompatibilityRule(
    val partType: String,
    val targetPartType: String?,
    val title: String,
    val description: String,
    val severity: String,
    val enabled: Boolean,
    val metadata: Map<String, String>?,
)

data class CompatibilityRuleUpdate(
    val partType: String? = null,
    val targetPartType: String? = null,
    val title: String? = null,
    val description: String? = null,
    val severity: String? = null,
    val enabled: Boolean? = null,
    val metadata: Map<String, String>? = null,
)

interface PcBuilderRepository {
    fun userExists(userId: Int): Boolean

    fun listMyBuilds(userId: Int, page: Int, limit: Int): PcBuildListResult

    fun listPopularBuilds(page: Int, limit: Int): PcBuildListResult

    fun createBuild(userId: Int, newBuild: NewPcBuild): PcBuildDetailRecord

    fun findBuildById(buildId: Int): PcBuildDetailRecord?

    fun findOwnedBuild(userId: Int, buildId: Int): PcBuildDetailRecord?

    fun findBuildByShareCode(shareCode: String): PcBuildDetailRecord?

    fun updateBuild(buildId: Int, update: PcBuildUpdate): PcBuildDetailRecord

    fun deleteBuild(buildId: Int): Boolean

    fun incrementViewCount(buildId: Int)

    fun assignShareCode(buildId: Int, shareCode: String): PcBuildDetailRecord

    fun productExists(productId: Int): Boolean

    fun findPriceCandidate(productId: Int, sellerId: Int? = null): PcBuildPartRecord?

    fun upsertBuildPart(buildId: Int, newPart: NewPcBuildPart): PcBuildDetailRecord

    fun removeBuildPart(buildId: Int, partId: Int): PcBuildDetailRecord?

    fun listCompatibilityRules(): List<CompatibilityRuleRecord>

    fun findCompatibilityRule(ruleId: Int): CompatibilityRuleRecord?

    fun createCompatibilityRule(newRule: NewCompatibilityRule): CompatibilityRuleRecord

    fun updateCompatibilityRule(ruleId: Int, update: CompatibilityRuleUpdate): CompatibilityRuleRecord

    fun deleteCompatibilityRule(ruleId: Int): Boolean
}
