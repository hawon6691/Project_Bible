package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun main(args: Array<String>) {
    require(args.isNotEmpty()) {
        "A DB command is required. Use one of: bootstrap, seed, init, smoke."
    }

    val config = PbShopConfig.from(HoconApplicationConfig(ConfigFactory.load()))
    val databaseFactory = DatabaseFactory(config.database)
    val dbCli = DbCli(databaseFactory, config)

    try {
        when (args.first()) {
            "bootstrap" -> dbCli.bootstrap()
            "seed" -> dbCli.seed()
            "init" -> dbCli.init()
            "smoke" -> dbCli.smoke()
            else -> error("Unsupported DB command: ${args.first()}")
        }
    } finally {
        databaseFactory.close()
    }
}

private class DbCli(
    private val databaseFactory: DatabaseFactory,
    private val config: PbShopConfig,
) {
    private val sqlDir: Path =
        Paths
            .get(System.getProperty("user.dir"))
            .toAbsolutePath()
            .resolve("..")
            .resolve("..")
            .resolve("..")
            .resolve("Database")
            .resolve("postgresql")
            .normalize()

    fun bootstrap() {
        noteManualSqlReference("setting.sql")
        resetPublicSchema()
        runSqlFile("postgres_table.sql")
        println("PBShop PostgreSQL bootstrap completed.")
    }

    fun seed() {
        runSqlFile("sample_data.sql")
        println("PBShop PostgreSQL sample data completed.")
    }

    fun init() {
        bootstrap()
        seed()
        println("PBShop PostgreSQL initialization completed.")
    }

    fun smoke() {
        databaseFactory.initialize()

        val requiredTables =
            listOf(
                "users",
                "categories",
                "products",
                "sellers",
                "price_entries",
            )

        databaseFactory.withConnection { connection ->
            connection.prepareStatement("SELECT 1").use { statement ->
                statement.executeQuery().use { resultSet ->
                    require(resultSet.next()) { "PostgreSQL smoke query returned no rows." }
                }
            }

            requiredTables.forEach { tableName ->
                connection.prepareStatement(
                    """
                    SELECT EXISTS (
                        SELECT 1
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name = ?
                    )
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, tableName)
                    statement.executeQuery().use { resultSet ->
                        require(resultSet.next() && resultSet.getBoolean(1)) {
                            "Required table not found: $tableName"
                        }
                    }
                }
            }

            requiredTables.forEach { tableName ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT COUNT(*) FROM $tableName").use { resultSet ->
                        require(resultSet.next() && resultSet.getInt(1) > 0) {
                            "Sample data validation failed for table: $tableName"
                        }
                    }
                }
            }
        }

        println("PBShop PostgreSQL smoke validation completed.")
        println("Engine: ${config.database.engine}")
        println("Database: ${config.database.database}")
        println("Tables: users, categories, products, sellers, price_entries")
    }

    private fun runSqlFile(fileName: String) {
        databaseFactory.initialize()

        val sqlFile = sqlDir.resolve(fileName)
        require(Files.exists(sqlFile)) {
            "SQL file not found: $sqlFile"
        }

        val script = Files.readString(sqlFile)
        val statements = splitSqlStatements(script)

        databaseFactory.withConnection { connection ->
            connection.autoCommit = true

            statements.forEachIndexed { index, statement ->
                val executableStatement = statement.trim()
                if (executableStatement.isBlank()) {
                    return@forEachIndexed
                }

                try {
                    connection.createStatement().use { jdbcStatement ->
                        jdbcStatement.execute(executableStatement)
                    }
                } catch (exception: Exception) {
                    throw IllegalStateException(
                        buildString {
                            append("Failed while executing ")
                            append(fileName)
                            append(" at statement ")
                            append(index + 1)
                            append(": ")
                            append(executableStatement.lineSequence().firstOrNull()?.trim().orEmpty())
                        },
                        exception,
                    )
                }
            }
        }

        println("Applied SQL file: $sqlFile")
    }

    private fun noteManualSqlReference(fileName: String) {
        val sqlFile = sqlDir.resolve(fileName)
        require(Files.exists(sqlFile)) {
            "SQL file not found: $sqlFile"
        }

        println("Manual SQL reference retained but skipped during automated bootstrap: $sqlFile")
    }

    private fun resetPublicSchema() {
        databaseFactory.initialize()

        databaseFactory.withConnection { connection ->
            connection.autoCommit = true
            connection.createStatement().use { statement ->
                statement.execute("DROP SCHEMA IF EXISTS public CASCADE")
                statement.execute("CREATE SCHEMA public")
            }
        }

        println("Reset public schema in ${config.database.database}.")
    }
}

private fun splitSqlStatements(script: String): List<String> {
    val statements = mutableListOf<String>()
    val current = StringBuilder()

    var singleQuoted = false
    var doubleQuoted = false
    var lineComment = false
    var blockComment = false
    var dollarQuotedTag: String? = null
    var index = 0

    while (index < script.length) {
        val currentChar = script[index]
        val nextChar = script.getOrNull(index + 1)

        if (lineComment) {
            current.append(currentChar)
            if (currentChar == '\n') {
                lineComment = false
            }
            index++
            continue
        }

        if (blockComment) {
            current.append(currentChar)
            if (currentChar == '*' && nextChar == '/') {
                current.append(nextChar)
                index += 2
                blockComment = false
                continue
            }
            index++
            continue
        }

        val activeDollarTag = dollarQuotedTag
        if (activeDollarTag != null) {
            if (script.startsWith(activeDollarTag, index)) {
                current.append(activeDollarTag)
                index += activeDollarTag.length
                dollarQuotedTag = null
                continue
            }

            current.append(currentChar)
            index++
            continue
        }

        if (singleQuoted) {
            current.append(currentChar)
            if (currentChar == '\'' && nextChar == '\'') {
                current.append(nextChar)
                index += 2
                continue
            }
            if (currentChar == '\'') {
                singleQuoted = false
            }
            index++
            continue
        }

        if (doubleQuoted) {
            current.append(currentChar)
            if (currentChar == '"') {
                doubleQuoted = false
            }
            index++
            continue
        }

        if (currentChar == '-' && nextChar == '-') {
            current.append(currentChar)
            current.append(nextChar)
            lineComment = true
            index += 2
            continue
        }

        if (currentChar == '/' && nextChar == '*') {
            current.append(currentChar)
            current.append(nextChar)
            blockComment = true
            index += 2
            continue
        }

        if (currentChar == '\'') {
            singleQuoted = true
            current.append(currentChar)
            index++
            continue
        }

        if (currentChar == '"') {
            doubleQuoted = true
            current.append(currentChar)
            index++
            continue
        }

        if (currentChar == '$') {
            val dollarTag = findDollarTag(script, index)
            if (dollarTag != null) {
                dollarQuotedTag = dollarTag
                current.append(dollarTag)
                index += dollarTag.length
                continue
            }
        }

        if (currentChar == ';') {
            val statement = current.toString().trim()
            if (statement.isNotBlank()) {
                statements += statement
            }
            current.setLength(0)
            index++
            continue
        }

        current.append(currentChar)
        index++
    }

    val tail = current.toString().trim()
    if (tail.isNotBlank()) {
        statements += tail
    }

    return statements
}

private fun findDollarTag(script: String, startIndex: Int): String? {
    if (script.startsWith("$$", startIndex)) {
        return "$$"
    }

    val remaining = script.substring(startIndex)
    val match = Regex("""^\$[A-Za-z_][A-Za-z0-9_]*\$""").find(remaining)
    return match?.value
}
