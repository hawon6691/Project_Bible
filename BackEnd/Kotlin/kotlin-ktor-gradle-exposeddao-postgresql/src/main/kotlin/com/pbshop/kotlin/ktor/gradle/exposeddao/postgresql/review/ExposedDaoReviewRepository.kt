package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ReviewEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ReviewsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.OrdersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoReviewRepository(
    private val databaseFactory: DatabaseFactory,
) : ReviewRepository {
    override fun listProductReviews(
        productId: Int,
        page: Int,
        limit: Int,
    ): ReviewListResult =
        databaseFactory.withTransaction {
            val filtered =
                ReviewsTable
                    .selectAll()
                    .where { (ReviewsTable.product eq productId) and ReviewsTable.deletedAt.isNull() }
                    .orderBy(ReviewsTable.createdAt to SortOrder.DESC, ReviewsTable.id to SortOrder.DESC)
                    .map(::toRecord)
            val offset = (page - 1) * limit
            ReviewListResult(filtered.drop(offset).take(limit), filtered.size)
        }

    override fun findReviewById(id: Int): ReviewRecord? =
        databaseFactory.withTransaction {
            ReviewsTable
                .selectAll()
                .where { (ReviewsTable.id eq id) and ReviewsTable.deletedAt.isNull() }
                .limit(1)
                .firstOrNull()
                ?.let(::toRecord)
        }

    override fun findReviewByUserAndOrder(
        userId: Int,
        orderId: Int,
    ): ReviewRecord? =
        databaseFactory.withTransaction {
            ReviewsTable
                .selectAll()
                .where { (ReviewsTable.user eq userId) and (ReviewsTable.order eq orderId) and ReviewsTable.deletedAt.isNull() }
                .limit(1)
                .firstOrNull()
                ?.let(::toRecord)
        }

    override fun createReview(
        userId: Int,
        newReview: NewReview,
    ): ReviewRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val created =
                ReviewEntity.new {
                    this.userId = EntityID(userId, UsersTable)
                    this.productId = EntityID(newReview.productId, ProductsTable)
                    this.orderId = EntityID(newReview.orderId, OrdersTable)
                    rating = newReview.rating.toShort()
                    content = newReview.content
                    isBest = false
                    createdAt = now
                    updatedAt = now
                    deletedAt = null
                }
            requireNotNull(findReviewById(created.id.value))
        }

    override fun updateReview(
        reviewId: Int,
        update: ReviewUpdate,
    ): ReviewRecord =
        databaseFactory.withTransaction {
            val entity = ReviewEntity.findById(reviewId)?.takeIf { it.deletedAt == null } ?: error("Review $reviewId not found")
            update.rating?.let { entity.rating = it.toShort() }
            update.content?.let { entity.content = it }
            entity.updatedAt = Instant.now()
            requireNotNull(findReviewById(reviewId))
        }

    override fun deleteReview(reviewId: Int) {
        databaseFactory.withTransaction {
            ReviewEntity.findById(reviewId)?.apply {
                deletedAt = Instant.now()
                updatedAt = Instant.now()
            } ?: error("Review $reviewId not found")
        }
    }

    private fun toRecord(row: org.jetbrains.exposed.sql.ResultRow): ReviewRecord =
        ReviewRecord(
            id = row[ReviewsTable.id].value,
            userId = row[ReviewsTable.user].value,
            productId = row[ReviewsTable.product].value,
            orderId = row[ReviewsTable.order].value,
            rating = row[ReviewsTable.rating].toInt(),
            content = row[ReviewsTable.content],
            isBest = row[ReviewsTable.isBest],
            createdAt = row[ReviewsTable.createdAt],
            updatedAt = row[ReviewsTable.updatedAt],
        )
}
