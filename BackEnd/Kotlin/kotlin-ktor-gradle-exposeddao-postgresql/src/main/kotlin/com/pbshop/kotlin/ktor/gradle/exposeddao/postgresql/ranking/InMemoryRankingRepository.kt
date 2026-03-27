package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

class InMemoryRankingRepository : RankingRepository {
    override fun listPopularProducts(
        categoryId: Int?,
        period: RankingPeriod,
        limit: Int,
    ): List<PopularProductRanking> {
        val items =
            listOf(
                PopularProductRanking(1, 1, "게이밍 노트북 A15", "https://img.example.com/products/1-thumb.jpg", 1490000, 15230, 2),
                PopularProductRanking(2, 2, "크리에이터북 16", "https://img.example.com/products/2-thumb.jpg", 1890000, 12990, -1),
                PopularProductRanking(3, 3, "초경량 노트북 Air", "https://img.example.com/products/3-thumb.jpg", 1190000, 11650, 0),
            )
        return items.take(limit)
    }

    override fun listPopularSearches(
        period: RankingPeriod,
        limit: Int,
    ): List<PopularSearchRanking> =
        listOf(
            PopularSearchRanking(1, "갤럭시북", 5230, 0),
            PopularSearchRanking(2, "맥북 프로", 4120, 1),
            PopularSearchRanking(3, "7800x3d", 2860, -1),
        ).take(limit)

    companion object {
        fun seeded(): InMemoryRankingRepository = InMemoryRankingRepository()
    }
}
