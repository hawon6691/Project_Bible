package com.pbshop.ktorshop_exposeddao

import com.pbshop.ktorshop_exposeddao.config.PbShopConfig
import com.pbshop.ktorshop_exposeddao.plugins.configureRouting
import com.pbshop.ktorshop_exposeddao.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = PbShopConfig.from(environment.config)

    install(CallLogging) {
        level = Level.INFO
    }

    configureSerialization()
    configureRouting(config)
}
