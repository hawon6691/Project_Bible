package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ShortformCommentsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ShortformLikesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ShortformProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ShortformsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoShortformRepository(
    private val databaseFactory: DatabaseFactory,
) : ShortformRepository {
    override fun userExists(userId: Int): Boolean =
        databaseFactory.withTransaction { !UsersTable.selectAll().where { UsersTable.id eq userId }.empty() }

    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction { !ProductsTable.selectAll().where { ProductsTable.id eq productId }.empty() }

    override fun createShortform(userId: Int, newShortform: NewShortform): ShortformRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val shortformId =
                ShortformsTable.insert {
                    it[user] = EntityID(userId, UsersTable)
                    it[title] = newShortform.title
                    it[videoUrl] = newShortform.videoUrl
                    it[thumbnailUrl] = newShortform.thumbnailUrl
                    it[durationSec] = newShortform.durationSec
                    it[viewCount] = 0
                    it[likeCount] = 0
                    it[commentCount] = 0
                    it[transcodeStatus] = newShortform.transcodeStatus
                    it[transcodedVideoUrl] = newShortform.transcodedVideoUrl
                    it[transcodeError] = newShortform.transcodeError
                    it[transcodedAt] = newShortform.transcodedAt
                    it[createdAt] = now
                    it[updatedAt] = now
                } get ShortformsTable.id
            newShortform.productIds.forEach { productId ->
                ShortformProductsTable.insert {
                    it[shortform] = shortformId
                    it[product] = EntityID(productId, ProductsTable)
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
            findShortformById(shortformId.value) ?: error("Shortform ${shortformId.value} not found")
        }

    override fun listFeed(cursor: Int?, limit: Int): ShortformFeedResult =
        databaseFactory.withTransaction {
            val rows =
                ShortformsTable.selectAll()
                    .where { cursor?.let { ShortformsTable.id less it } ?: org.jetbrains.exposed.sql.Op.TRUE }
                    .orderBy(ShortformsTable.id to SortOrder.DESC)
                    .limit(limit)
                    .toList()
            ShortformFeedResult(items = rows.map(::toShortformRecord), nextCursor = rows.lastOrNull()?.get(ShortformsTable.id)?.value)
        }

    override fun findShortformById(shortformId: Int): ShortformRecord? =
        databaseFactory.withTransaction {
            ShortformsTable.selectAll().where { ShortformsTable.id eq shortformId }.singleOrNull()?.let(::toShortformRecord)
        }

    override fun incrementViewCount(shortformId: Int): ShortformRecord? =
        databaseFactory.withTransaction {
            val current = ShortformsTable.selectAll().where { ShortformsTable.id eq shortformId }.singleOrNull() ?: return@withTransaction null
            ShortformsTable.update({ ShortformsTable.id eq shortformId }) {
                it[viewCount] = current[ShortformsTable.viewCount] + 1
                it[updatedAt] = Instant.now()
            }
            findShortformById(shortformId)
        }

    override fun toggleLike(userId: Int, shortformId: Int): Pair<Boolean, Int> =
        databaseFactory.withTransaction {
            val existing =
                ShortformLikesTable.selectAll()
                    .where { (ShortformLikesTable.shortform eq shortformId) and (ShortformLikesTable.user eq userId) }
                    .singleOrNull()
            val liked =
                if (existing == null) {
                    val now = Instant.now()
                    ShortformLikesTable.insert {
                        it[shortform] = EntityID(shortformId, ShortformsTable)
                        it[user] = EntityID(userId, UsersTable)
                        it[createdAt] = now
                        it[updatedAt] = now
                    }
                    true
                } else {
                    ShortformLikesTable.deleteWhere { ShortformLikesTable.id eq existing[ShortformLikesTable.id].value }
                    false
                }
            val count = ShortformLikesTable.selectAll().where { ShortformLikesTable.shortform eq shortformId }.count().toInt()
            ShortformsTable.update({ ShortformsTable.id eq shortformId }) {
                it[likeCount] = count
                it[updatedAt] = Instant.now()
            }
            liked to count
        }

    override fun createComment(userId: Int, shortformId: Int, content: String): ShortformCommentRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val commentId =
                ShortformCommentsTable.insert {
                    it[shortform] = EntityID(shortformId, ShortformsTable)
                    it[user] = EntityID(userId, UsersTable)
                    it[ShortformCommentsTable.content] = content
                    it[createdAt] = now
                    it[updatedAt] = now
                } get ShortformCommentsTable.id
            val count = ShortformCommentsTable.selectAll().where { ShortformCommentsTable.shortform eq shortformId }.count().toInt()
            ShortformsTable.update({ ShortformsTable.id eq shortformId }) {
                it[commentCount] = count
                it[updatedAt] = now
            }
            ShortformCommentsTable.innerJoin(UsersTable)
                .selectAll()
                .where { ShortformCommentsTable.id eq commentId.value }
                .single()
                .let(::toCommentRecord)
        }

    override fun listComments(shortformId: Int, page: Int, limit: Int): ShortformPageResult<ShortformCommentRecord> =
        databaseFactory.withTransaction {
            val rows =
                ShortformCommentsTable.innerJoin(UsersTable)
                    .selectAll()
                    .where { ShortformCommentsTable.shortform eq shortformId }
                    .orderBy(ShortformCommentsTable.createdAt to SortOrder.DESC)
                    .toList()
            val offset = (page - 1).coerceAtLeast(0) * limit
            ShortformPageResult(rows.drop(offset).take(limit).map(::toCommentRecord), rows.size)
        }

    override fun listRanking(limit: Int): List<ShortformRecord> =
        databaseFactory.withTransaction {
            ShortformsTable.selectAll()
                .orderBy(ShortformsTable.likeCount to SortOrder.DESC, ShortformsTable.viewCount to SortOrder.DESC)
                .limit(limit)
                .map(::toShortformRecord)
        }

    override fun updateTranscodeStatus(shortformId: Int, status: String, error: String?, transcodedVideoUrl: String?, transcodedAt: Instant?): ShortformRecord =
        databaseFactory.withTransaction {
            ShortformsTable.update({ ShortformsTable.id eq shortformId }) {
                it[transcodeStatus] = status
                it[transcodeError] = error
                it[ShortformsTable.transcodedVideoUrl] = transcodedVideoUrl
                it[ShortformsTable.transcodedAt] = transcodedAt
                it[updatedAt] = Instant.now()
            }
            findShortformById(shortformId) ?: error("Shortform $shortformId not found")
        }

    override fun deleteShortform(shortformId: Int): Boolean =
        databaseFactory.withTransaction {
            ShortformCommentsTable.deleteWhere { ShortformCommentsTable.shortform eq shortformId }
            ShortformLikesTable.deleteWhere { ShortformLikesTable.shortform eq shortformId }
            ShortformProductsTable.deleteWhere { ShortformProductsTable.shortform eq shortformId }
            ShortformsTable.deleteWhere { ShortformsTable.id eq shortformId } > 0
        }

    override fun listUserShortforms(userId: Int, page: Int, limit: Int): ShortformPageResult<ShortformRecord> =
        databaseFactory.withTransaction {
            val rows =
                ShortformsTable.selectAll()
                    .where { ShortformsTable.user eq userId }
                    .orderBy(ShortformsTable.createdAt to SortOrder.DESC)
                    .toList()
            val offset = (page - 1).coerceAtLeast(0) * limit
            ShortformPageResult(rows.drop(offset).take(limit).map(::toShortformRecord), rows.size)
        }

    private fun toShortformRecord(row: ResultRow): ShortformRecord {
        val user = loadUser(row[ShortformsTable.user].value)
        val products = loadProducts(row[ShortformsTable.id].value)
        return ShortformRecord(
            id = row[ShortformsTable.id].value,
            userId = row[ShortformsTable.user].value,
            title = row[ShortformsTable.title],
            videoUrl = row[ShortformsTable.videoUrl],
            thumbnailUrl = row[ShortformsTable.thumbnailUrl],
            durationSec = row[ShortformsTable.durationSec],
            viewCount = row[ShortformsTable.viewCount],
            likeCount = row[ShortformsTable.likeCount],
            commentCount = row[ShortformsTable.commentCount],
            transcodeStatus = row[ShortformsTable.transcodeStatus],
            transcodedVideoUrl = row[ShortformsTable.transcodedVideoUrl],
            transcodeError = row[ShortformsTable.transcodeError],
            transcodedAt = row[ShortformsTable.transcodedAt],
            createdAt = row[ShortformsTable.createdAt],
            updatedAt = row[ShortformsTable.updatedAt],
            user = user,
            products = products,
        )
    }

    private fun toCommentRecord(row: ResultRow): ShortformCommentRecord =
        ShortformCommentRecord(
            id = row[ShortformCommentsTable.id].value,
            shortformId = row[ShortformCommentsTable.shortform].value,
            userId = row[ShortformCommentsTable.user].value,
            content = row[ShortformCommentsTable.content],
            createdAt = row[ShortformCommentsTable.createdAt],
            updatedAt = row[ShortformCommentsTable.updatedAt],
            user = loadUser(row[ShortformCommentsTable.user].value),
        )

    private fun loadUser(userId: Int): ShortformUserRecord {
        val row = UsersTable.selectAll().where { UsersTable.id eq userId }.single()
        return ShortformUserRecord(
            id = row[UsersTable.id].value,
            name = row[UsersTable.name],
            nickname = row[UsersTable.nickname],
            profileImageUrl = row[UsersTable.profileImageUrl],
        )
    }

    private fun loadProducts(shortformId: Int): List<ShortformProductRecord> =
        ShortformProductsTable.innerJoin(ProductsTable)
            .selectAll()
            .where { ShortformProductsTable.shortform eq shortformId }
            .map { row ->
                ShortformProductRecord(
                    id = row[ProductsTable.id].value,
                    name = row[ProductsTable.name],
                    thumbnailUrl = row[ProductsTable.thumbnailUrl],
                    lowestPrice = row[ProductsTable.lowestPrice] ?: row[ProductsTable.price],
                )
            }
}
