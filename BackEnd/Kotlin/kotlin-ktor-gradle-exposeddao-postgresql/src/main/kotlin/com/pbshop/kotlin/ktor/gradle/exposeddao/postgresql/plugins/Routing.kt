package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.builder.BuilderController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.builder.BuilderRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.builder.BuilderService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.catalog.CatalogController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.catalog.CatalogRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.catalog.CatalogService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.commerce.CommerceController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.commerce.CommerceRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.commerce.CommerceService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.discovery.DiscoveryController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.discovery.DiscoveryRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.discovery.DiscoveryService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs.pbShopEndpointSpecs
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.engagement.EngagementController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.engagement.EngagementRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.engagement.EngagementService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.MediaController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.MediaRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media.MediaService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ops.OpsController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ops.OpsRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ops.OpsService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.platform.HealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.platform.PlatformController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.platform.PlatformService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserController
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user.UserService
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(
    config: PbShopConfig,
    dbHealthService: DbHealthService,
) {
    val endpointSpecs = pbShopEndpointSpecs()
    val platformController =
        PlatformController(
            platformService = PlatformService(config, endpointSpecs),
            healthService = HealthService(config, dbHealthService),
        )
    val authController = AuthController(AuthService(AuthRepository()))
    val userController = UserController(UserService(UserRepository()))
    val catalogController = CatalogController(CatalogService(CatalogRepository()))
    val commerceController = CommerceController(CommerceService(CommerceRepository()))
    val engagementController = EngagementController(EngagementService(EngagementRepository()))
    val discoveryController = DiscoveryController(DiscoveryService(DiscoveryRepository()))
    val mediaController = MediaController(MediaService(MediaRepository()))
    val builderController = BuilderController(BuilderService(BuilderRepository()))
    val opsController = OpsController(OpsService(OpsRepository()))

    routing {
        with(platformController) {
            registerPublicRoutes()
        }

        route(config.apiPrefix) {
            with(platformController) {
                registerApiRoutes()
            }
            with(authController) {
                register()
            }
            with(userController) {
                register()
            }
            with(catalogController) {
                register()
            }
            with(commerceController) {
                register()
            }
            with(engagementController) {
                register()
            }
            with(discoveryController) {
                register()
            }
            with(mediaController) {
                register()
            }
            with(builderController) {
                register()
            }
            with(opsController) {
                register()
            }
        }
    }
}
