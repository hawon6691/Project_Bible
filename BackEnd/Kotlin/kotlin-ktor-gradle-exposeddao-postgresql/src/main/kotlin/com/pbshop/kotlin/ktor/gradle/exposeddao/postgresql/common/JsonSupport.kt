package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun Any?.toJsonElement(): JsonElement =
    when (this) {
        null -> JsonNull
        is JsonElement -> this
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is Enum<*> -> JsonPrimitive(name)
        is Map<*, *> ->
            JsonObject(
                entries.associate { (key, value) ->
                    key.toString() to value.toJsonElement()
                },
            )
        is Iterable<*> -> JsonArray(map { it.toJsonElement() })
        is Array<*> -> JsonArray(map { it.toJsonElement() })
        else -> JsonPrimitive(toString())
    }

fun pageMeta(
    page: Int = 1,
    limit: Int = 20,
    totalCount: Int,
): JsonElement =
    mapOf(
        "page" to page,
        "limit" to limit,
        "totalCount" to totalCount,
        "totalPages" to if (limit <= 0) 1 else ((totalCount + limit - 1) / limit),
    ).toJsonElement()
