package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.InquiriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.InquiryEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoInquiryRepository(
    private val databaseFactory: DatabaseFactory,
) : InquiryRepository {
    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable.selectAll().where { ProductsTable.id eq productId }.limit(1).empty()
        }

    override fun listProductInquiries(
        productId: Int,
        page: Int,
        limit: Int,
    ): InquiryListResult =
        databaseFactory.withTransaction {
            val rows =
                InquiriesTable.selectAll()
                    .where { InquiriesTable.product eq productId }
                    .orderBy(InquiriesTable.createdAt to SortOrder.DESC, InquiriesTable.id to SortOrder.DESC)
                    .map(::toRecord)
            val offset = (page - 1) * limit
            InquiryListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun listUserInquiries(
        userId: Int,
        page: Int,
        limit: Int,
    ): InquiryListResult =
        databaseFactory.withTransaction {
            val rows =
                InquiriesTable.selectAll()
                    .where { InquiriesTable.user eq userId }
                    .orderBy(InquiriesTable.createdAt to SortOrder.DESC, InquiriesTable.id to SortOrder.DESC)
                    .map(::toRecord)
            val offset = (page - 1) * limit
            InquiryListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun findInquiryById(id: Int): InquiryRecord? =
        databaseFactory.withTransaction {
            InquiriesTable.selectAll()
                .where { InquiriesTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.let(::toRecord)
        }

    override fun createInquiry(
        userId: Int,
        newInquiry: NewInquiry,
    ): InquiryRecord =
        databaseFactory.withTransaction {
            val created =
                InquiryEntity.new {
                    productId = EntityID(newInquiry.productId, ProductsTable)
                    this.userId = EntityID(userId, UsersTable)
                    title = newInquiry.title
                    content = newInquiry.content
                    isSecret = newInquiry.isSecret
                    answer = null
                    answeredBy = null
                    answeredAt = null
                    createdAt = Instant.now()
                }
            InquiryRecord(
                id = created.id.value,
                productId = created.productId.value,
                userId = created.userId.value,
                title = created.title,
                content = created.content,
                isSecret = created.isSecret,
                answer = created.answer,
                answeredBy = created.answeredBy?.value,
                answeredAt = created.answeredAt,
                createdAt = created.createdAt,
            )
        }

    override fun answerInquiry(
        inquiryId: Int,
        answeredBy: Int,
        answer: String,
    ): InquiryRecord =
        databaseFactory.withTransaction {
            val entity = requireNotNull(InquiryEntity.findById(inquiryId)) { "Inquiry $inquiryId not found" }
            entity.answer = answer
            entity.answeredBy = EntityID(answeredBy, UsersTable)
            entity.answeredAt = Instant.now()
            InquiryRecord(
                id = entity.id.value,
                productId = entity.productId.value,
                userId = entity.userId.value,
                title = entity.title,
                content = entity.content,
                isSecret = entity.isSecret,
                answer = entity.answer,
                answeredBy = entity.answeredBy?.value,
                answeredAt = entity.answeredAt,
                createdAt = entity.createdAt,
            )
        }

    override fun deleteInquiry(inquiryId: Int) {
        databaseFactory.withTransaction {
            InquiryEntity.findById(inquiryId)?.delete()
        }
    }

    private fun toRecord(row: org.jetbrains.exposed.sql.ResultRow): InquiryRecord =
        InquiryRecord(
            id = row[InquiriesTable.id].value,
            productId = row[InquiriesTable.product].value,
            userId = row[InquiriesTable.user].value,
            title = row[InquiriesTable.title],
            content = row[InquiriesTable.content],
            isSecret = row[InquiriesTable.isSecret],
            answer = row[InquiriesTable.answer],
            answeredBy = row[InquiriesTable.answeredBy]?.value,
            answeredAt = row[InquiriesTable.answeredAt],
            createdAt = row[InquiriesTable.createdAt],
        )
}
