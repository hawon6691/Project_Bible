package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import java.time.Instant

class InMemoryInquiryRepository private constructor(
    private val products: MutableSet<Int>,
    private val inquiries: MutableList<InquiryRecord>,
) : InquiryRepository {
    private var nextId: Int = (inquiries.maxOfOrNull { it.id } ?: 0) + 1

    override fun productExists(productId: Int): Boolean = products.contains(productId)

    override fun listProductInquiries(
        productId: Int,
        page: Int,
        limit: Int,
    ): InquiryListResult {
        val filtered = inquiries.filter { it.productId == productId }.sortedByDescending { it.createdAt }
        val offset = (page - 1) * limit
        return InquiryListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun listUserInquiries(
        userId: Int,
        page: Int,
        limit: Int,
    ): InquiryListResult {
        val filtered = inquiries.filter { it.userId == userId }.sortedByDescending { it.createdAt }
        val offset = (page - 1) * limit
        return InquiryListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findInquiryById(id: Int): InquiryRecord? = inquiries.firstOrNull { it.id == id }

    override fun createInquiry(
        userId: Int,
        newInquiry: NewInquiry,
    ): InquiryRecord {
        val created =
            InquiryRecord(
                id = nextId++,
                productId = newInquiry.productId,
                userId = userId,
                title = newInquiry.title,
                content = newInquiry.content,
                isSecret = newInquiry.isSecret,
                answer = null,
                answeredBy = null,
                answeredAt = null,
                createdAt = Instant.now(),
            )
        inquiries += created
        return created
    }

    override fun answerInquiry(
        inquiryId: Int,
        answeredBy: Int,
        answer: String,
    ): InquiryRecord {
        val index = inquiries.indexOfFirst { it.id == inquiryId }
        check(index >= 0) { "Inquiry $inquiryId not found" }
        val updated =
            inquiries[index].copy(
                answer = answer,
                answeredBy = answeredBy,
                answeredAt = Instant.now(),
            )
        inquiries[index] = updated
        return updated
    }

    override fun deleteInquiry(inquiryId: Int) {
        inquiries.removeIf { it.id == inquiryId }
    }

    companion object {
        fun seeded(): InMemoryInquiryRepository =
            InMemoryInquiryRepository(
                products = mutableSetOf(1, 2, 3, 4),
                inquiries =
                    mutableListOf(
                        InquiryRecord(
                            id = 1,
                            productId = 1,
                            userId = 4,
                            title = "램 업그레이드 가능 여부",
                            content = "기본 16GB에서 추후 32GB 업그레이드 가능한가요?",
                            isSecret = false,
                            answer = "네, 공식 서비스센터에서 업그레이드 가능합니다.",
                            answeredBy = 1,
                            answeredAt = Instant.now().minusSeconds(7_200),
                            createdAt = Instant.now().minusSeconds(14_400),
                        ),
                    ),
            )
    }
}
