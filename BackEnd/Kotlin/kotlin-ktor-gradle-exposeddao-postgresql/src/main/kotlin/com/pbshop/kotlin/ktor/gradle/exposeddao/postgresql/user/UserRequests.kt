package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateMeRequest(
    val name: String? = null,
    val phone: String? = null,
    val password: String? = null,
)

@Serializable
data class UserStatusUpdateRequest(
    val status: String,
)

@Serializable
data class UserProfileUpdateRequest(
    val nickname: String? = null,
    val bio: String? = null,
)
