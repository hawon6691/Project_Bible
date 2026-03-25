package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

data class SpecDefinitionRecord(
    val id: Int,
    val categoryId: Int,
    val categoryName: String?,
    val name: String,
    val type: String,
    val options: List<String>?,
    val unit: String?,
    val isComparable: Boolean,
    val dataType: String,
    val sortOrder: Int,
)

data class ProductSpecRecord(
    val specDefinitionId: Int,
    val name: String,
    val value: String,
    val numericValue: Double?,
)

data class SpecScoreRecord(
    val id: Int,
    val specDefinitionId: Int,
    val value: String,
    val score: Int,
    val benchmarkSource: String?,
)

data class ComparedProductRecord(
    val productId: Int,
    val productName: String,
    val specs: List<ProductSpecRecord>,
)

data class NewSpecDefinition(
    val categoryId: Int,
    val name: String,
    val type: String,
    val options: List<String>?,
    val unit: String?,
    val isComparable: Boolean,
    val dataType: String,
    val sortOrder: Int,
)

data class SpecDefinitionUpdate(
    val categoryId: Int?,
    val name: String?,
    val type: String?,
    val options: List<String>?,
    val unit: String?,
    val isComparable: Boolean?,
    val dataType: String?,
    val sortOrder: Int?,
)

data class NewProductSpecValue(
    val specDefinitionId: Int,
    val value: String,
    val numericValue: Double?,
)

data class NewSpecScore(
    val value: String,
    val score: Int,
    val benchmarkSource: String?,
)

interface SpecRepository {
    fun listDefinitions(categoryId: Int? = null): List<SpecDefinitionRecord>

    fun findDefinitionById(id: Int): SpecDefinitionRecord?

    fun createDefinition(newDefinition: NewSpecDefinition): SpecDefinitionRecord

    fun updateDefinition(
        id: Int,
        update: SpecDefinitionUpdate,
    ): SpecDefinitionRecord

    fun deleteDefinition(id: Int)

    fun definitionInUse(id: Int): Boolean

    fun categoryExists(categoryId: Int): Boolean

    fun productExists(productId: Int): Boolean

    fun listProductSpecs(productId: Int): List<ProductSpecRecord>

    fun replaceProductSpecs(
        productId: Int,
        values: List<NewProductSpecValue>,
    ): List<ProductSpecRecord>

    fun listComparedProducts(productIds: List<Int>): List<ComparedProductRecord>

    fun listSpecScores(specDefinitionId: Int): List<SpecScoreRecord>

    fun replaceSpecScores(
        specDefinitionId: Int,
        values: List<NewSpecScore>,
    ): List<SpecScoreRecord>
}
