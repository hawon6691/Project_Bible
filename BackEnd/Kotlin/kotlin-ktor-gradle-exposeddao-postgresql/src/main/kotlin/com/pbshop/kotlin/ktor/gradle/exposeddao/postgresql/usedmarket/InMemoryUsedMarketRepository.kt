package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket

class InMemoryUsedMarketRepository(
    private val products: Map<Int, List<UsedPriceSummaryRecord>>,
    private val categories: Map<Int, List<UsedPriceSummaryRecord>>,
    private val buildEstimates: Map<Pair<Int, Int>, UsedBuildEstimateRecord>,
) : UsedMarketRepository {
    override fun productPrice(productId: Int): UsedPriceSummaryRecord? = products[productId]?.lastOrNull()

    override fun categoryPrices(categoryId: Int, page: Int, limit: Int): UsedPricePageResult {
        val items = categories[categoryId].orEmpty()
        val from = ((page - 1) * limit).coerceAtMost(items.size)
        val to = (from + limit).coerceAtMost(items.size)
        return UsedPricePageResult(items.subList(from, to), items.size)
    }

    override fun estimateBuild(userId: Int, buildId: Int): UsedBuildEstimateRecord? = buildEstimates[userId to buildId]

    companion object {
        fun seeded(): InMemoryUsedMarketRepository {
            val p1History =
                listOf(
                    UsedPriceSummaryRecord(1, "게이밍 노트북 A15", 1180000, 980000, 1320000, 8, "DOWN"),
                    UsedPriceSummaryRecord(1, "게이밍 노트북 A15", 1120000, 940000, 1250000, 11, "STABLE"),
                )
            val p2History =
                listOf(
                    UsedPriceSummaryRecord(2, "사무용 노트북 Slim", 640000, 550000, 720000, 5, "UP"),
                )
            val buildEstimate =
                UsedBuildEstimateRecord(
                    buildId = 1,
                    estimatedPrice = 1320000,
                    partBreakdown =
                        listOf(
                            UsedBuildEstimatePartRecord("CPU", 1, "AMD Ryzen 7 7800X3D", 1, 280000),
                            UsedBuildEstimatePartRecord("GPU", 2, "RTX 4070 SUPER", 1, 680000),
                            UsedBuildEstimatePartRecord("RAM", 3, "DDR5 32GB", 1, 120000),
                            UsedBuildEstimatePartRecord("MOTHERBOARD", 4, "B650 메인보드", 1, 140000),
                            UsedBuildEstimatePartRecord("SSD", 5, "NVMe SSD 1TB", 1, 100000),
                        ),
                )
            return InMemoryUsedMarketRepository(
                products = mapOf(1 to p1History, 2 to p2History),
                categories = mapOf(2 to listOf(p1History.last(), p2History.last())),
                buildEstimates = mapOf((4 to 1) to buildEstimate),
            )
        }
    }
}
