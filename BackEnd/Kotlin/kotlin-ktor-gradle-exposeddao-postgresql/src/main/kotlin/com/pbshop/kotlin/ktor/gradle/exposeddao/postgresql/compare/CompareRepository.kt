package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

data class CompareItemRecord(
    val productId: Int,
    val name: String,
    val slug: String,
    val thumbnailUrl: String?,
)

data class CompareDetailItemRecord(
    val productId: Int,
    val name: String,
    val slug: String,
    val categoryId: Int,
    val bestPrice: Int,
    val ratingAvg: Double,
    val specs: Map<String, String>,
)

interface CompareRepository {
    fun productExists(productId: Int): Boolean

    fun add(compareKey: String, productId: Int)

    fun remove(compareKey: String, productId: Int)

    fun list(compareKey: String): List<CompareItemRecord>

    fun detail(compareKey: String): List<CompareDetailItemRecord>
}
