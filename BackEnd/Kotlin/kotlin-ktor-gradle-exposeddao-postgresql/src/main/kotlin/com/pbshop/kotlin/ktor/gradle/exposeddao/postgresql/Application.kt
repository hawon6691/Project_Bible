package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.JdbcDbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureRouting
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = PbShopConfig.from(environment.config)
    val databaseFactory = DatabaseFactory(config.database)
    databaseFactory.initialize()
    module(
        dbHealthService = JdbcDbHealthService(
            databaseFactory = databaseFactory,
            config = config.database,
        ),
    )
}

fun Application.module(dbHealthService: DbHealthService) {
    val config = PbShopConfig.from(environment.config)

    install(CallLogging) {
        level = Level.INFO
    }

    configureSerialization()
    configureRouting(config, dbHealthService)
}
