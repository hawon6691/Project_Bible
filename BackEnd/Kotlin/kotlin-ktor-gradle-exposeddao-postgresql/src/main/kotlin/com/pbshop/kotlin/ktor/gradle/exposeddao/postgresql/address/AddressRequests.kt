package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

import kotlinx.serialization.Serializable

@Serializable
data class AddressRequest(
    val label: String,
    val recipientName: String,
    val phone: String,
    val zipCode: String,
    val address: String,
    val addressDetail: String? = null,
    val isDefault: Boolean = false,
)

@Serializable
data class AddressUpdateRequest(
    val label: String? = null,
    val recipientName: String? = null,
    val phone: String? = null,
    val zipCode: String? = null,
    val address: String? = null,
    val addressDetail: String? = null,
    val isDefault: Boolean? = null,
)
