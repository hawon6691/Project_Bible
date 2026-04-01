package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

data class AutoModelRecord(
    val id: Int,
    val brand: String,
    val name: String,
    val type: String,
    val year: Int,
    val basePrice: Int,
    val imageUrl: String?,
    val isActive: Boolean,
)

data class AutoTrimRecord(
    val id: Int,
    val modelId: Int,
    val name: String,
    val basePrice: Int,
)

data class AutoOptionRecord(
    val id: Int,
    val trimId: Int,
    val name: String,
    val price: Int,
)

data class LeaseOfferRecord(
    val id: Int,
    val modelId: Int,
    val company: String,
    val type: String,
    val monthlyPayment: Int,
    val deposit: Int,
    val contractMonths: Int,
    val annualMileage: Int,
    val isActive: Boolean,
)

interface AutoRepository {
    fun listModels(brand: String?, type: String?): List<AutoModelRecord>

    fun findModelById(modelId: Int): AutoModelRecord?

    fun listTrims(modelId: Int): List<AutoTrimRecord>

    fun listOptionsForTrimIds(trimIds: List<Int>): List<AutoOptionRecord>

    fun listLeaseOffers(modelId: Int): List<LeaseOfferRecord>
}
