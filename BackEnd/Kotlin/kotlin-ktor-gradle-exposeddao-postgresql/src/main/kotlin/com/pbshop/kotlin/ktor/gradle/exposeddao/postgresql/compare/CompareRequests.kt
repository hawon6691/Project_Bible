package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

import kotlinx.serialization.Serializable

@Serializable
data class AddCompareItemRequest(
    val productId: Int,
)
