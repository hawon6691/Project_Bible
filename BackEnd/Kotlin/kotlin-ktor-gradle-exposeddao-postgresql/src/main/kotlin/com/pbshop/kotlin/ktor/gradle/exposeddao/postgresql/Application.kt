package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.JdbcAuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.CategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category.JdbcCategoryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.JdbcDbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureHttp
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureRouting
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins.configureSerialization
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.JdbcUserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserRepository
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
        authRepository = JdbcAuthRepository(databaseFactory),
        userRepository = JdbcUserRepository(databaseFactory),
        categoryRepository = JdbcCategoryRepository(databaseFactory),
    )
}

fun Application.module(
    dbHealthService: DbHealthService,
    authRepository: AuthRepository,
    userRepository: UserRepository,
    categoryRepository: CategoryRepository,
) {
    val config = PbShopConfig.from(environment.config)

    install(CallLogging) {
        level = Level.INFO
    }

    configureSerialization()
    configureHttp(config)
    configureRouting(config, dbHealthService, authRepository, userRepository, categoryRepository)
}
