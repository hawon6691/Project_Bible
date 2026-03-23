package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class ProductController(
    private val service: ProductService,
) {
    fun Route.register() {
        get("/products") {
            call.respondStub(
                service.listProducts(
                    ProductQueryRequest(
                        page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                        limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                        categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull(),
                        search = call.request.queryParameters["search"],
                        minPrice = call.request.queryParameters["minPrice"]?.toIntOrNull(),
                        maxPrice = call.request.queryParameters["maxPrice"]?.toIntOrNull(),
                        sort = call.request.queryParameters["sort"],
                        specs = call.request.queryParameters["specs"],
                    ),
                ),
            )
        }
        get("/products/{id}") {
            call.respondStub(service.detail(call.productId()))
        }
        post("/products") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.create(call.receive()))
        }
        patch("/products/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.update(call.productId(), call.receive()))
        }
        delete("/products/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.productId()))
        }
        post("/products/{id}/options") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.addOption(call.productId(), call.receive()))
        }
        patch("/products/{id}/options/{optionId}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateOption(call.productId(), call.optionId(), call.receive()))
        }
        delete("/products/{id}/options/{optionId}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deleteOption(call.productId(), call.optionId()))
        }
        post("/products/{id}/images") {
            call.requireAnyRole(PbRole.ADMIN)
            val multipart = call.receiveMultipart()
            var fileName: String? = null
            var isMain = false
            var sortOrder = 0
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> fileName = part.originalFileName ?: part.name ?: "product-image.bin"
                    is PartData.FormItem -> {
                        when (part.name) {
                            "isMain" -> isMain = part.value.equals("true", ignoreCase = true)
                            "sortOrder" -> sortOrder = part.value.toIntOrNull() ?: 0
                        }
                    }
                    else -> Unit
                }
                part.dispose()
            }
            val upload =
                ProductImageUploadRequest(
                    fileName =
                        fileName
                            ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "업로드할 이미지 파일이 필요합니다."),
                    isMain = isMain,
                    sortOrder = sortOrder,
                )
            call.respondStub(service.addImage(call.productId(), upload))
        }
        delete("/products/{id}/images/{imageId}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deleteImage(call.productId(), call.imageId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.productId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.optionId(): Int = parameters["optionId"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.imageId(): Int = parameters["imageId"]?.toIntOrNull() ?: 0
}
