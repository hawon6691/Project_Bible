package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Transaction
import java.sql.Connection
import javax.sql.DataSource

class DatabaseFactory(
    private val config: DatabaseConfig,
) {
    @Volatile
    private var dataSource: HikariDataSource? = null

    @Volatile
    private var database: Database? = null

    @Synchronized
    fun initialize() {
        if (dataSource != null && database != null) {
            return
        }

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            username = config.username
            password = config.password
            driverClassName = config.driver
            maximumPoolSize = config.maxPoolSize
            minimumIdle = config.minIdle
            poolName = "pbshop-kotlin-postgresql"
            isAutoCommit = true
            initializationFailTimeout = -1
            connectionTestQuery = "SELECT 1"
        }

        val createdDataSource = HikariDataSource(hikariConfig)
        val exposedDatabase = Database.connect(createdDataSource)

        dataSource = createdDataSource
        database = exposedDatabase
    }

    fun dataSource(): DataSource {
        initialize()
        return checkNotNull(dataSource) { "Database data source is not initialized." }
    }

    fun database(): Database {
        initialize()
        return checkNotNull(database) { "Exposed database is not initialized." }
    }

    fun <T> withTransaction(block: Transaction.() -> T): T = transaction(database(), block)

    fun <T> withConnection(block: (Connection) -> T): T =
        dataSource().connection.use(block)

    fun close() {
        dataSource?.close()
        dataSource = null
        database = null
    }
}
