package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode

class InMemoryErrorCodeRepository(
    private val items: List<ErrorCodeRecord> = ERROR_CODE_CATALOG,
) : ErrorCodeRepository {
    override fun listCodes(): List<ErrorCodeRecord> = items

    override fun findCode(key: String): ErrorCodeRecord? = items.firstOrNull { it.key == key || it.code == key }
}

class CatalogErrorCodeRepository : ErrorCodeRepository {
    override fun listCodes(): List<ErrorCodeRecord> = ERROR_CODE_CATALOG

    override fun findCode(key: String): ErrorCodeRecord? = ERROR_CODE_CATALOG.firstOrNull { it.key == key || it.code == key }
}
