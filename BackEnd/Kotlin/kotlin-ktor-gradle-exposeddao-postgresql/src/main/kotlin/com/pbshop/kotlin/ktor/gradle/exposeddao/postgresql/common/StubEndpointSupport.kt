package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun endpoint(
    method: HttpMethod,
    path: String,
    tag: String,
    summary: String,
    roles: Set<PbRole> = emptySet(),
    handler: (EndpointRequest) -> EndpointResponse,
): StubOperation =
    StubOperation(
        spec =
            EndpointSpec(
                method = method,
                path = path,
                tag = tag,
                summary = summary,
                roles = roles,
            ),
        handler = handler,
    )

fun paged(
    items: List<Any?>,
    totalCount: Int = items.size,
    page: Int = 1,
    limit: Int = 20,
): EndpointResponse =
    EndpointResponse(
        data = items,
        meta =
            mapOf(
                "page" to page,
                "limit" to limit,
                "totalCount" to totalCount,
                "totalPages" to if (limit <= 0) 1 else ((totalCount + limit - 1) / limit),
            ),
    )

fun message(
    text: String,
    vararg extras: Pair<String, Any?>,
): EndpointResponse =
    EndpointResponse(
        data =
            linkedMapOf<String, Any?>(
                "message" to text,
            ).apply {
                extras.forEach { (key, value) -> put(key, value) }
            },
    )

fun productSummary(
    id: Int,
    name: String,
): Map<String, Any?> =
    mapOf(
        "id" to id,
        "name" to name,
        "lowestPrice" to 1590000,
        "thumbnailUrl" to "/images/products/$id-thumb.webp",
        "categoryName" to "Laptop",
    )
