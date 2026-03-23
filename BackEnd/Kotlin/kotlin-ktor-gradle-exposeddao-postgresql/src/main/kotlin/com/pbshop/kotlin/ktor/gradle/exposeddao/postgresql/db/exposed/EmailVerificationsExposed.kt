package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthVerificationType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object EmailVerificationsTable : IntIdTable("email_verifications") {
    val user = reference("user_id", UsersTable)
    val type = pgEnum<AuthVerificationType>("type", "verification_type")
    val code = varchar("code", 6)
    val attemptCount = integer("attempt_count")
    val isUsed = bool("is_used")
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
}

class EmailVerificationEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EmailVerificationEntity>(EmailVerificationsTable)

    var userId by EmailVerificationsTable.user
    var type by EmailVerificationsTable.type
    var code by EmailVerificationsTable.code
    var attemptCount by EmailVerificationsTable.attemptCount
    var isUsed by EmailVerificationsTable.isUsed
    var expiresAt by EmailVerificationsTable.expiresAt
    var createdAt by EmailVerificationsTable.createdAt
}
