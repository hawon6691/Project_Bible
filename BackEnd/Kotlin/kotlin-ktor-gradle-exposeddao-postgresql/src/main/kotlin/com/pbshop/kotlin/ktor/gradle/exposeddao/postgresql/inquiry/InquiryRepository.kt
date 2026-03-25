package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import java.time.Instant

data class InquiryRecord(
    val id: Int,
    val productId: Int,
    val userId: Int,
    val title: String,
    val content: String,
    val isSecret: Boolean,
    val answer: String?,
    val answeredBy: Int?,
    val answeredAt: Instant?,
    val createdAt: Instant,
)

data class InquiryListResult(
    val items: List<InquiryRecord>,
    val totalCount: Int,
)

data class NewInquiry(
    val productId: Int,
    val title: String,
    val content: String,
    val isSecret: Boolean,
)

interface InquiryRepository {
    fun productExists(productId: Int): Boolean

    fun listProductInquiries(
        productId: Int,
        page: Int,
        limit: Int,
    ): InquiryListResult

    fun listUserInquiries(
        userId: Int,
        page: Int,
        limit: Int,
    ): InquiryListResult

    fun findInquiryById(id: Int): InquiryRecord?

    fun createInquiry(
        userId: Int,
        newInquiry: NewInquiry,
    ): InquiryRecord

    fun answerInquiry(
        inquiryId: Int,
        answeredBy: Int,
        answer: String,
    ): InquiryRecord

    fun deleteInquiry(inquiryId: Int)
}
