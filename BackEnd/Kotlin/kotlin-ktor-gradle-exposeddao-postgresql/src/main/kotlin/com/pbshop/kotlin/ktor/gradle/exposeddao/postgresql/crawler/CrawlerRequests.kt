package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

import kotlinx.serialization.Serializable

@Serializable
data class CrawlerJobCreateRequest(
    val sellerId: Int,
    val name: String,
    val cronExpression: String? = null,
    val collectPrice: Boolean = true,
    val collectSpec: Boolean = true,
    val detectAnomaly: Boolean = true,
    val isActive: Boolean = true,
)

@Serializable
data class CrawlerJobUpdateRequest(
    val sellerId: Int? = null,
    val name: String? = null,
    val cronExpression: String? = null,
    val collectPrice: Boolean? = null,
    val collectSpec: Boolean? = null,
    val detectAnomaly: Boolean? = null,
    val isActive: Boolean? = null,
)

@Serializable
data class CrawlerTriggerRequest(
    val jobId: Int? = null,
    val sellerId: Int? = null,
    val name: String? = null,
)
