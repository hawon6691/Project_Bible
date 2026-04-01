package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode

interface ErrorCodeRepository {
    fun listCodes(): List<ErrorCodeRecord>

    fun findCode(key: String): ErrorCodeRecord?
}
