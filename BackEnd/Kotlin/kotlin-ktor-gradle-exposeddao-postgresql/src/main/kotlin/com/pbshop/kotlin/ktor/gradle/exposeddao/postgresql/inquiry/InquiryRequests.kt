package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import kotlinx.serialization.Serializable

@Serializable
data class InquiryRequest(
    val title: String,
    val content: String,
    val isSecret: Boolean = false,
)

@Serializable
data class InquiryAnswerRequest(
    val answer: String,
)
