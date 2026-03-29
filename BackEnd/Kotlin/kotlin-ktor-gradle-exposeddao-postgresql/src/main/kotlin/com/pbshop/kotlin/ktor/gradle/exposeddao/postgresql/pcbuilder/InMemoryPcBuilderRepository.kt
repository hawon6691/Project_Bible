package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder

import java.time.Instant

class InMemoryPcBuilderRepository(
    private val userIds: Set<Int>,
    products: List<PcBuildProductRecord>,
    builds: List<PcBuildDetailRecord>,
    parts: List<PcBuildPartRecord>,
    rules: List<CompatibilityRuleRecord>,
) : PcBuilderRepository {
    private val products = products.associateBy { it.id }.toMutableMap()
    private val builds = linkedMapOf<Int, PcBuildDetailRecord>()
    private val parts = linkedMapOf<Int, PcBuildPartRecord>()
    private val rules = linkedMapOf<Int, CompatibilityRuleRecord>()
    private val candidateParts = mutableListOf<PcBuildPartRecord>()
    private var buildSequence = 1
    private var partSequence = 1
    private var ruleSequence = 1

    init {
        builds.forEach {
            this.builds[it.id] = it
            buildSequence = maxOf(buildSequence, it.id + 1)
        }
        parts.forEach {
            if (it.buildId == 0) {
                candidateParts += it
            } else {
                this.parts[it.id] = it
                partSequence = maxOf(partSequence, it.id + 1)
            }
        }
        rules.forEach {
            this.rules[it.id] = it
            ruleSequence = maxOf(ruleSequence, it.id + 1)
        }
    }

    override fun userExists(userId: Int): Boolean = userIds.contains(userId)

    override fun listMyBuilds(userId: Int, page: Int, limit: Int): PcBuildListResult =
        paginate(builds.values.filter { it.userId == userId }, page, limit)

    override fun listPopularBuilds(page: Int, limit: Int): PcBuildListResult =
        paginate(builds.values.sortedWith(compareByDescending<PcBuildDetailRecord> { it.viewCount }.thenByDescending { it.updatedAt }), page, limit)

    override fun createBuild(userId: Int, newBuild: NewPcBuild): PcBuildDetailRecord {
        val now = Instant.now()
        val saved =
            PcBuildDetailRecord(
                id = buildSequence++,
                userId = userId,
                name = newBuild.name,
                description = newBuild.description,
                purpose = newBuild.purpose,
                budget = newBuild.budget,
                totalPrice = 0,
                shareCode = null,
                viewCount = 0,
                parts = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
        builds[saved.id] = saved
        return saved
    }

    override fun findBuildById(buildId: Int): PcBuildDetailRecord? = builds[buildId]?.withParts()

    override fun findOwnedBuild(userId: Int, buildId: Int): PcBuildDetailRecord? = builds[buildId]?.takeIf { it.userId == userId }?.withParts()

    override fun findBuildByShareCode(shareCode: String): PcBuildDetailRecord? =
        builds.values.firstOrNull { it.shareCode == shareCode }?.withParts()

    override fun updateBuild(buildId: Int, update: PcBuildUpdate): PcBuildDetailRecord {
        val current = builds[buildId] ?: error("Build $buildId not found")
        val updated =
            current.copy(
                name = update.name ?: current.name,
                description = update.description ?: current.description,
                purpose = update.purpose ?: current.purpose,
                budget = update.budget ?: current.budget,
                updatedAt = Instant.now(),
            )
        builds[buildId] = updated
        return updated.withParts()
    }

    override fun deleteBuild(buildId: Int): Boolean {
        val existed = builds.remove(buildId) != null
        if (existed) {
            parts.entries.removeIf { it.value.buildId == buildId }
        }
        return existed
    }

    override fun incrementViewCount(buildId: Int) {
        builds[buildId]?.let { builds[buildId] = it.copy(viewCount = it.viewCount + 1, updatedAt = Instant.now()) }
    }

    override fun assignShareCode(buildId: Int, shareCode: String): PcBuildDetailRecord {
        val current = builds[buildId] ?: error("Build $buildId not found")
        val updated = current.copy(shareCode = shareCode, updatedAt = Instant.now())
        builds[buildId] = updated
        return updated.withParts()
    }

    override fun productExists(productId: Int): Boolean = products.containsKey(productId)

    override fun findPriceCandidate(productId: Int, sellerId: Int?): PcBuildPartRecord? {
        val candidates = candidateParts.filter { it.productId == productId }
        if (candidates.isEmpty()) return null
        return sellerId?.let { id -> candidates.firstOrNull { it.sellerId == id } } ?: candidates.minByOrNull { it.unitPrice }
    }

    override fun upsertBuildPart(buildId: Int, newPart: NewPcBuildPart): PcBuildDetailRecord {
        val currentBuild = builds[buildId] ?: error("Build $buildId not found")
        val candidate = findPriceCandidate(newPart.productId, newPart.sellerId) ?: error("Price candidate not found")
        val existing = parts.values.firstOrNull { it.buildId == buildId && it.partType == newPart.partType }
        val saved =
            PcBuildPartRecord(
                id = existing?.id ?: partSequence++,
                buildId = buildId,
                productId = newPart.productId,
                sellerId = candidate.sellerId,
                partType = newPart.partType,
                quantity = newPart.quantity,
                unitPrice = candidate.unitPrice,
                totalPrice = candidate.unitPrice * newPart.quantity,
                product = candidate.product,
                seller = candidate.seller,
            )
        parts[saved.id] = saved
        builds[buildId] = currentBuild.copy(totalPrice = buildParts(buildId).sumOf { it.totalPrice }, updatedAt = Instant.now())
        return builds[buildId]!!.withParts()
    }

    override fun removeBuildPart(buildId: Int, partId: Int): PcBuildDetailRecord? {
        val target = parts[partId]?.takeIf { it.buildId == buildId } ?: return null
        parts.remove(target.id)
        val currentBuild = builds[buildId] ?: return null
        builds[buildId] = currentBuild.copy(totalPrice = buildParts(buildId).sumOf { it.totalPrice }, updatedAt = Instant.now())
        return builds[buildId]!!.withParts()
    }

    override fun listCompatibilityRules(): List<CompatibilityRuleRecord> = rules.values.sortedBy { it.id }

    override fun findCompatibilityRule(ruleId: Int): CompatibilityRuleRecord? = rules[ruleId]

    override fun createCompatibilityRule(newRule: NewCompatibilityRule): CompatibilityRuleRecord {
        val now = Instant.now()
        val saved =
            CompatibilityRuleRecord(
                id = ruleSequence++,
                partType = newRule.partType,
                targetPartType = newRule.targetPartType,
                title = newRule.title,
                description = newRule.description,
                severity = newRule.severity,
                enabled = newRule.enabled,
                metadata = newRule.metadata,
                createdAt = now,
                updatedAt = now,
            )
        rules[saved.id] = saved
        return saved
    }

    override fun updateCompatibilityRule(ruleId: Int, update: CompatibilityRuleUpdate): CompatibilityRuleRecord {
        val current = rules[ruleId] ?: error("Rule $ruleId not found")
        val updated =
            current.copy(
                partType = update.partType ?: current.partType,
                targetPartType = update.targetPartType ?: current.targetPartType,
                title = update.title ?: current.title,
                description = update.description ?: current.description,
                severity = update.severity ?: current.severity,
                enabled = update.enabled ?: current.enabled,
                metadata = update.metadata ?: current.metadata,
                updatedAt = Instant.now(),
            )
        rules[ruleId] = updated
        return updated
    }

    override fun deleteCompatibilityRule(ruleId: Int): Boolean = rules.remove(ruleId) != null

    private fun PcBuildDetailRecord.withParts(): PcBuildDetailRecord = copy(parts = buildParts(id), totalPrice = buildParts(id).sumOf { it.totalPrice })

    private fun buildParts(buildId: Int): List<PcBuildPartRecord> = parts.values.filter { it.buildId == buildId }.sortedBy { it.id }

    private fun paginate(records: List<PcBuildDetailRecord>, page: Int, limit: Int): PcBuildListResult {
        val sorted = records.sortedByDescending { it.updatedAt }
        val offset = (page - 1).coerceAtLeast(0) * limit
        return PcBuildListResult(
            items =
                sorted.drop(offset).take(limit).map {
                    PcBuildSummaryRecord(
                        id = it.id,
                        userId = it.userId,
                        name = it.name,
                        description = it.description,
                        purpose = it.purpose,
                        budget = it.budget,
                        totalPrice = it.withParts().totalPrice,
                        shareCode = it.shareCode,
                        viewCount = it.viewCount,
                        parts = buildParts(it.id),
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
            totalCount = sorted.size,
        )
    }

    companion object {
        fun seeded(): InMemoryPcBuilderRepository {
            val now = Instant.parse("2026-03-10T09:00:00Z")
            val cpu = PcBuildProductRecord(1, "AMD Ryzen 7 7800X3D", "/products/cpu.jpg", 350000)
            val gpu = PcBuildProductRecord(2, "RTX 4070 SUPER", "/products/gpu.jpg", 920000)
            val ram = PcBuildProductRecord(3, "DDR5 32GB", "/products/ram.jpg", 150000)
            val board = PcBuildProductRecord(4, "B650 메인보드", "/products/board.jpg", 220000)
            val ssd = PcBuildProductRecord(5, "NVMe SSD 1TB", "/products/ssd.jpg", 170000)
            val psu = PcBuildProductRecord(6, "850W PSU", "/products/psu.jpg", 120000)
            val `case` = PcBuildProductRecord(7, "미들타워 케이스", "/products/case.jpg", 90000)

            val candidates =
                listOf(
                    PcBuildPartRecord(0, 0, 1, 1, "CPU", 1, 350000, 350000, cpu, PcBuildSellerRecord(1, "쿠팡", 350000)),
                    PcBuildPartRecord(0, 0, 2, 1, "GPU", 1, 920000, 920000, gpu, PcBuildSellerRecord(1, "쿠팡", 920000)),
                    PcBuildPartRecord(0, 0, 3, 2, "RAM", 1, 150000, 150000, ram, PcBuildSellerRecord(2, "네이버", 150000)),
                    PcBuildPartRecord(0, 0, 4, 2, "MOTHERBOARD", 1, 220000, 220000, board, PcBuildSellerRecord(2, "네이버", 220000)),
                    PcBuildPartRecord(0, 0, 5, 1, "SSD", 1, 170000, 170000, ssd, PcBuildSellerRecord(1, "쿠팡", 170000)),
                    PcBuildPartRecord(0, 0, 6, 1, "PSU", 1, 120000, 120000, psu, PcBuildSellerRecord(1, "쿠팡", 120000)),
                    PcBuildPartRecord(0, 0, 7, 2, "CASE", 1, 90000, 90000, `case`, PcBuildSellerRecord(2, "네이버", 90000)),
                )
            val build1 =
                PcBuildDetailRecord(1, 4, "게이밍 PC 2026", "고사양 게이밍 데스크탑", "GAMING", 3000000, 1810000, null, 18, emptyList(), now.minusSeconds(172800), now.minusSeconds(3600))
            val build2 =
                PcBuildDetailRecord(2, 5, "사무용 PC", "오피스 업무용", "OFFICE", 1200000, 760000, "OFFICE123456", 31, emptyList(), now.minusSeconds(259200), now.minusSeconds(7200))
            val seededParts =
                listOf(
                    candidates[0].copy(id = 1, buildId = 1),
                    candidates[1].copy(id = 2, buildId = 1),
                    candidates[2].copy(id = 3, buildId = 1),
                    candidates[3].copy(id = 4, buildId = 1),
                    candidates[4].copy(id = 5, buildId = 1),
                    candidates[5].copy(id = 6, buildId = 2),
                    candidates[6].copy(id = 7, buildId = 2),
                )
            val rules =
                listOf(
                    CompatibilityRuleRecord(1, "CPU", "MOTHERBOARD", "CPU 소켓 체크", "메인보드가 선택되지 않았습니다.", "HIGH", true, mapOf("type" to "socket"), now, now),
                    CompatibilityRuleRecord(2, "GPU", "PSU", "전원 용량 체크", "GPU를 위해 PSU를 선택해주세요.", "MEDIUM", true, mapOf("type" to "power"), now, now),
                )
            return InMemoryPcBuilderRepository(
                userIds = setOf(1, 4, 5, 6),
                products = listOf(cpu, gpu, ram, board, ssd, psu, `case`),
                builds = listOf(build1, build2),
                parts = seededParts + candidates,
                rules = rules,
            )
        }
    }
}
