package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.BoardEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.BoardsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CommentEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CommentsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PostEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PostLikeEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PostLikesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PostsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ExposedDaoCommunityRepository(
    private val databaseFactory: DatabaseFactory,
) : CommunityRepository {
    override fun listBoards(): List<BoardRecord> =
        databaseFactory.withTransaction {
            BoardsTable.selectAll().orderBy(BoardsTable.sortOrder to SortOrder.ASC).map {
                BoardRecord(it[BoardsTable.id].value, it[BoardsTable.name], it[BoardsTable.slug], it[BoardsTable.description], it[BoardsTable.sortOrder], it[BoardsTable.isActive])
            }
        }

    override fun boardExists(boardId: Int): Boolean =
        databaseFactory.withTransaction { !BoardsTable.selectAll().where { BoardsTable.id eq boardId }.limit(1).empty() }

    override fun listPosts(boardId: Int, page: Int, limit: Int, search: String?, sort: String?): PostListResult =
        databaseFactory.withTransaction {
            var query =
                PostsTable.innerJoin(UsersTable)
                    .selectAll()
                    .where { (PostsTable.board eq boardId) and PostsTable.deletedAt.isNull() }
            search?.takeIf { it.isNotBlank() }?.let { q ->
                query = PostsTable.innerJoin(UsersTable).selectAll().where {
                    (PostsTable.board eq boardId) and PostsTable.deletedAt.isNull() and ((PostsTable.title like "%$q%") or (PostsTable.content like "%$q%"))
                }
            }
            val rows =
                query.map {
                    PostSummaryRecord(
                        it[PostsTable.id].value,
                        it[PostsTable.board].value,
                        it[PostsTable.user].value,
                        it[UsersTable.name],
                        it[PostsTable.title],
                        it[PostsTable.viewCount],
                        it[PostsTable.likeCount],
                        it[PostsTable.commentCount],
                        it[PostsTable.createdAt],
                    )
                }.let {
                    when (sort?.lowercase()) {
                        "popular" -> it.sortedByDescending { row -> row.likeCount }
                        "most_commented" -> it.sortedByDescending { row -> row.commentCount }
                        else -> it.sortedByDescending { row -> row.createdAt }
                    }
                }
            val offset = (page - 1) * limit
            PostListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun findPostDetail(postId: Int): PostDetailRecord? =
        databaseFactory.withTransaction {
            val row =
                PostsTable.innerJoin(UsersTable)
                    .selectAll()
                    .where { (PostsTable.id eq postId) and PostsTable.deletedAt.isNull() }
                    .limit(1)
                    .firstOrNull()
                    ?: return@withTransaction null
            PostDetailRecord(
                row[PostsTable.id].value,
                row[PostsTable.board].value,
                row[PostsTable.user].value,
                row[UsersTable.name],
                row[PostsTable.title],
                row[PostsTable.content],
                row[PostsTable.viewCount],
                row[PostsTable.likeCount],
                row[PostsTable.commentCount],
                row[PostsTable.createdAt],
                listComments(row[PostsTable.id].value),
            )
        }

    override fun findPostSummary(postId: Int): PostSummaryRecord? =
        databaseFactory.withTransaction {
            PostsTable.innerJoin(UsersTable).selectAll().where { (PostsTable.id eq postId) and PostsTable.deletedAt.isNull() }.limit(1).firstOrNull()?.let {
                PostSummaryRecord(it[PostsTable.id].value, it[PostsTable.board].value, it[PostsTable.user].value, it[UsersTable.name], it[PostsTable.title], it[PostsTable.viewCount], it[PostsTable.likeCount], it[PostsTable.commentCount], it[PostsTable.createdAt])
            }
        }

    override fun createPost(userId: Int, newPost: NewPost): PostDetailRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val created =
                PostEntity.new {
                    boardId = EntityID(newPost.boardId, BoardsTable)
                    this.userId = EntityID(userId, UsersTable)
                    title = newPost.title
                    content = newPost.content
                    viewCount = 0
                    likeCount = 0
                    commentCount = 0
                    createdAt = now
                    updatedAt = now
                    deletedAt = null
                }
            requireNotNull(findPostDetail(created.id.value))
        }

    override fun updatePost(postId: Int, update: PostUpdate): PostDetailRecord =
        databaseFactory.withTransaction {
            val entity = PostEntity.findById(postId)?.takeIf { it.deletedAt == null } ?: error("Post $postId not found")
            update.title?.let { entity.title = it }
            update.content?.let { entity.content = it }
            entity.updatedAt = Instant.now()
            requireNotNull(findPostDetail(postId))
        }

    override fun deletePost(postId: Int) {
        databaseFactory.withTransaction {
            PostEntity.findById(postId)?.apply {
                deletedAt = Instant.now()
                updatedAt = Instant.now()
            }
        }
    }

    override fun toggleLike(userId: Int, postId: Int): Pair<Boolean, Int> =
        databaseFactory.withTransaction {
            val existing = PostLikeEntity.find { (PostLikesTable.post eq postId) and (PostLikesTable.user eq userId) }.firstOrNull()
            val liked =
                if (existing == null) {
                    PostLikeEntity.new {
                        this.postId = EntityID(postId, PostsTable)
                        this.userId = EntityID(userId, UsersTable)
                        createdAt = Instant.now()
                    }
                    true
                } else {
                    existing.delete()
                    false
                }
            val count = PostLikeEntity.find { PostLikesTable.post eq postId }.count().toInt()
            PostEntity.findById(postId)?.apply { likeCount = count }
            liked to count
        }

    override fun listComments(postId: Int): List<CommentRecord> =
        databaseFactory.withTransaction {
            CommentsTable.innerJoin(UsersTable)
                .selectAll()
                .where { (CommentsTable.post eq postId) and CommentsTable.deletedAt.isNull() }
                .orderBy(CommentsTable.id to SortOrder.ASC)
                .map {
                    CommentRecord(it[CommentsTable.id].value, it[CommentsTable.post].value, it[CommentsTable.user].value, it[UsersTable.name], it[CommentsTable.parent]?.value, it[CommentsTable.content], it[CommentsTable.createdAt])
                }
        }

    override fun findCommentById(commentId: Int): CommentRecord? =
        databaseFactory.withTransaction {
            CommentsTable.innerJoin(UsersTable)
                .selectAll()
                .where { (CommentsTable.id eq commentId) and CommentsTable.deletedAt.isNull() }
                .limit(1)
                .firstOrNull()
                ?.let {
                    CommentRecord(it[CommentsTable.id].value, it[CommentsTable.post].value, it[CommentsTable.user].value, it[UsersTable.name], it[CommentsTable.parent]?.value, it[CommentsTable.content], it[CommentsTable.createdAt])
                }
        }

    override fun createComment(userId: Int, postId: Int, newComment: NewComment): CommentRecord =
        databaseFactory.withTransaction {
            val created =
                CommentEntity.new {
                    this.postId = EntityID(postId, PostsTable)
                    this.userId = EntityID(userId, UsersTable)
                    this.parentId = newComment.parentId?.let { EntityID(it, CommentsTable) }
                    content = newComment.content
                    createdAt = Instant.now()
                    deletedAt = null
                }
            PostEntity.findById(postId)?.apply { commentCount = CommentEntity.find { (CommentsTable.post eq postId) and CommentsTable.deletedAt.isNull() }.count().toInt() }
            requireNotNull(findCommentById(created.id.value))
        }

    override fun deleteComment(commentId: Int) {
        databaseFactory.withTransaction {
            val entity = CommentEntity.findById(commentId) ?: return@withTransaction
            val postId = entity.postId.value
            entity.deletedAt = Instant.now()
            PostEntity.findById(postId)?.apply { commentCount = CommentEntity.find { (CommentsTable.post eq postId) and CommentsTable.deletedAt.isNull() }.count().toInt() }
        }
    }
}
