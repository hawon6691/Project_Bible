package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal

import kotlinx.serialization.Serializable

@Serializable
data class DealProductRequest(
    val productId: Int,
    val dealPrice: Int,
    val stock: Int,
)

@Serializable
data class DealCreateRequest(
    val title: String,
    val type: String = "SPECIAL",
    val description: String? = null,
    val discountRate: Int,
    val startDate: String,
    val endDate: String,
    val bannerUrl: String? = null,
    val products: List<DealProductRequest>,
)

@Serializable
data class DealUpdateRequest(
    val title: String? = null,
    val type: String? = null,
    val description: String? = null,
    val discountRate: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val isActive: Boolean? = null,
    val bannerUrl: String? = null,
    val products: List<DealProductRequest>? = null,
)
