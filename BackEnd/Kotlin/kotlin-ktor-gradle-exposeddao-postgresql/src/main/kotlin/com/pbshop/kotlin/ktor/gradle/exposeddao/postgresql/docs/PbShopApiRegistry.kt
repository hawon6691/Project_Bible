package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.authOperations
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.builder.builderOperations
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.catalog.catalogOperations
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.commerce.commerceOperations
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.EndpointSpec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.discovery.discoveryOperations
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.engagement.engagementOperations
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.mediaOperations
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ops.opsOperations
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.userOperations

fun pbShopEndpointOperations(): List<StubOperation> =
    authOperations() +
        userOperations() +
        catalogOperations() +
        commerceOperations() +
        engagementOperations() +
        discoveryOperations() +
        mediaOperations() +
        builderOperations() +
        opsOperations()

fun pbShopEndpointSpecs(): List<EndpointSpec> = pbShopEndpointOperations().map { it.spec }
