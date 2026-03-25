package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import kotlinx.serialization.Serializable

data class SupportTicketCreateRequest(
    val category: String,
    val title: String,
    val content: String,
    val attachments: List<String> = emptyList(),
)

@Serializable
data class SupportReplyRequest(
    val content: String,
)

@Serializable
data class SupportStatusUpdateRequest(
    val status: String,
)

@Serializable
data class FaqRequest(
    val category: String,
    val question: String,
    val answer: String,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)

@Serializable
data class FaqUpdateRequest(
    val category: String? = null,
    val question: String? = null,
    val answer: String? = null,
    val sortOrder: Int? = null,
    val isActive: Boolean? = null,
)

@Serializable
data class NoticeRequest(
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
)

@Serializable
data class NoticeUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    val isPinned: Boolean? = null,
)
