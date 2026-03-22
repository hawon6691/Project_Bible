package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.DatabaseConfig

interface DbHealthService {
    fun check(): DbHealthCheckResult
}

data class DbHealthCheckResult(
    val status: String,
    val engine: String,
    val database: String,
    val message: String? = null,
) {
    val isUp: Boolean
        get() = status == "UP"

    companion object {
        fun up(
            engine: String,
            database: String,
        ): DbHealthCheckResult =
            DbHealthCheckResult(
                status = "UP",
                engine = engine,
                database = database,
            )

        fun down(
            engine: String,
            database: String,
            message: String,
        ): DbHealthCheckResult =
            DbHealthCheckResult(
                status = "DOWN",
                engine = engine,
                database = database,
                message = message,
            )
    }
}

class JdbcDbHealthService(
    private val databaseFactory: DatabaseFactory,
    private val config: DatabaseConfig,
) : DbHealthService {
    override fun check(): DbHealthCheckResult =
        try {
            databaseFactory.withConnection { connection ->
                connection.prepareStatement("SELECT 1").use { statement ->
                    statement.executeQuery().use { resultSet ->
                        require(resultSet.next()) { "DB health query returned no rows." }
                    }
                }
            }

            DbHealthCheckResult.up(
                engine = config.engine,
                database = config.database,
            )
        } catch (exception: Exception) {
            DbHealthCheckResult.down(
                engine = config.engine,
                database = config.database,
                message = exception.message ?: exception::class.simpleName.orEmpty(),
            )
        }
}
