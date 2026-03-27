package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search

import kotlinx.serialization.Serializable

@Serializable
data class SaveRecentSearchRequest(
    val keyword: String,
)

@Serializable
data class SearchPreferenceRequest(
    val saveRecentSearches: Boolean,
)

@Serializable
data class SearchWeightRequest(
    val nameWeight: Double,
    val keywordWeight: Double,
    val clickWeight: Double,
)
