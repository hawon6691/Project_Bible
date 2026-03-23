package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api.buildOpenApiDocument
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api.buildSwaggerHtml
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api.pbShopEndpoints
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun main(args: Array<String>) {
    require(args.isNotEmpty()) {
        "A docs command is required. Use: docs."
    }
    require(args.first() == "docs") {
        "Unsupported docs command: ${args.first()}"
    }

    val config = PbShopConfig.from(HoconApplicationConfig(ConfigFactory.load()))
    val outputDir: Path =
        Paths
            .get(System.getProperty("user.dir"))
            .toAbsolutePath()
            .resolve("build")
            .resolve("docs")
            .normalize()

    Files.createDirectories(outputDir)
    val json = Json { prettyPrint = true }

    Files.writeString(
        outputDir.resolve("openapi.json"),
        json.encodeToString(buildOpenApiDocument(config, pbShopEndpoints())),
    )
    Files.writeString(
        outputDir.resolve("swagger.html"),
        buildSwaggerHtml(config),
    )

    println("Exported docs to $outputDir")
}
