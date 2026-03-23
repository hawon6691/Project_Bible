package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject

inline fun <reified T : Enum<T>> Table.pgEnum(
    name: String,
    pgType: String,
): Column<T> =
    customEnumeration(
        name = name,
        sql = pgType,
        fromDb = { value ->
            when (value) {
                is PGobject -> enumValueOf<T>(value.value!!)
                is String -> enumValueOf<T>(value)
                else -> error("Unexpected value for enum $pgType: $value")
            }
        },
        toDb = { value ->
            PGobject().apply {
                type = pgType
                this.value = value.name
            }
        },
    )
