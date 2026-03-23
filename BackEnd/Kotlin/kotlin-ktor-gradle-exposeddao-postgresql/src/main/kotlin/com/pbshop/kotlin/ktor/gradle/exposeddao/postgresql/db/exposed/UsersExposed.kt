package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthUserStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : IntIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val name = varchar("name", 50)
    val phone = varchar("phone", 20)
    val role = pgEnum<PbRole>("role", "user_role")
    val status = pgEnum<AuthUserStatus>("status", "user_status")
    val emailVerified = bool("email_verified")
    val emailVerifiedAt = timestamp("email_verified_at").nullable()
    val nickname = varchar("nickname", 30).uniqueIndex()
    val bio = varchar("bio", 200).nullable()
    val profileImageUrl = varchar("profile_image_url", 500).nullable()
    val point = integer("point")
    val refreshToken = varchar("refresh_token", 500).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var password by UsersTable.password
    var name by UsersTable.name
    var phone by UsersTable.phone
    var role by UsersTable.role
    var status by UsersTable.status
    var emailVerified by UsersTable.emailVerified
    var emailVerifiedAt by UsersTable.emailVerifiedAt
    var nickname by UsersTable.nickname
    var bio by UsersTable.bio
    var profileImageUrl by UsersTable.profileImageUrl
    var point by UsersTable.point
    var refreshToken by UsersTable.refreshToken
    var createdAt by UsersTable.createdAt
    var updatedAt by UsersTable.updatedAt
    var deletedAt by UsersTable.deletedAt
}
