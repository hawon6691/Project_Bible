package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class I18nApiTest {
    @Test
    fun i18n_translation_exchange_rate_and_convert_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val translations = client.get("/api/v1/i18n/translations?locale=en&namespace=product") { pbHeaders(clientId = "i18n-translations") }
        val upsertTranslation =
            client.post("/api/v1/admin/i18n/translations") {
                pbHeaders(role = "ADMIN", clientId = "i18n-translation-upsert")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"locale":"ja","namespace":"product","key":"product.buy_now","value":"今すぐ購入"}""")
            }
        val exchangeRates = client.get("/api/v1/i18n/exchange-rates") { pbHeaders(clientId = "i18n-rates") }
        val upsertRate =
            client.post("/api/v1/admin/i18n/exchange-rates") {
                pbHeaders(role = "ADMIN", clientId = "i18n-rate-upsert")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"baseCurrency":"USD","targetCurrency":"JPY","rate":149.8}""")
            }
        val convert = client.get("/api/v1/i18n/convert?amount=100&from=USD&to=JPY") { pbHeaders(clientId = "i18n-convert") }
        val createdTranslationId =
            Json.parseToJsonElement(upsertTranslation.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content
        val deleteTranslation =
            client.delete("/api/v1/admin/i18n/translations/$createdTranslationId") {
                pbHeaders(role = "ADMIN", clientId = "i18n-translation-delete")
            }

        assertEquals(HttpStatusCode.OK, translations.status)
        assertEquals(HttpStatusCode.Created, upsertTranslation.status)
        assertEquals(HttpStatusCode.OK, exchangeRates.status)
        assertEquals(HttpStatusCode.Created, upsertRate.status)
        assertEquals(HttpStatusCode.OK, convert.status)
        assertEquals(HttpStatusCode.OK, deleteTranslation.status)
        assertTrue(translations.bodyAsText().contains("\"product.lowest_price\""))
        assertTrue(upsertRate.bodyAsText().contains("\"JPY\""))
        assertTrue(convert.bodyAsText().contains("\"targetCurrency\": \"JPY\""))
    }
}

class ImageApiTest {
    @Test
    fun image_upload_variants_delete_and_legacy_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val upload =
            client.post("/api/v1/images/upload") {
                pbHeaders(role = "USER", clientId = "image-upload")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("category", "product")
                            append(
                                "file",
                                "fake-image".encodeToByteArray(),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=sample-image.jpg")
                                    append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                                },
                            )
                        },
                    ),
                )
            }
        val imageId =
            Json.parseToJsonElement(upload.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val variants = client.get("/api/v1/images/$imageId/variants") { pbHeaders(clientId = "image-variants") }
        val legacyUpload =
            client.post("/api/v1/upload/image") {
                pbHeaders(role = "USER", clientId = "image-legacy")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "file",
                                "legacy".encodeToByteArray(),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=legacy-image.png")
                                    append(HttpHeaders.ContentType, ContentType.Image.PNG.toString())
                                },
                            )
                        },
                    ),
                )
            }
        val delete = client.delete("/api/v1/images/$imageId") { pbHeaders(role = "ADMIN", clientId = "image-delete") }

        assertEquals(HttpStatusCode.Created, upload.status)
        assertEquals(HttpStatusCode.OK, variants.status)
        assertEquals(HttpStatusCode.Created, legacyUpload.status)
        assertEquals(HttpStatusCode.OK, delete.status)
        assertTrue(upload.bodyAsText().contains("\"processingStatus\": \"COMPLETED\""))
        assertTrue(variants.bodyAsText().contains("\"THUMBNAIL\""))
        assertTrue(legacyUpload.bodyAsText().contains("\"url\""))
    }

    @Test
    fun product_and_user_image_routes_reuse_the_image_flow() = testApplication {
        installPbShopApp()

        val productImage =
            client.post("/api/v1/products/1/images") {
                pbHeaders(role = "ADMIN", clientId = "product-image-attach")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("isMain", "true")
                            append("sortOrder", "3")
                            append(
                                "file",
                                "product-image".encodeToByteArray(),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=product-detail.jpg")
                                    append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                                },
                            )
                        },
                    ),
                )
            }
        val profileImage =
            client.post("/api/v1/users/me/profile-image") {
                pbHeaders(role = "USER", clientId = "profile-image")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "file",
                                "profile-image".encodeToByteArray(),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=profile.webp")
                                    append(HttpHeaders.ContentType, ContentType.parse("image/webp").toString())
                                },
                            )
                        },
                    ),
                )
            }
        val profileDelete = client.delete("/api/v1/users/me/profile-image") { pbHeaders(role = "USER", clientId = "profile-image-delete") }

        assertEquals(HttpStatusCode.Created, productImage.status)
        assertEquals(HttpStatusCode.OK, profileImage.status)
        assertEquals(HttpStatusCode.OK, profileDelete.status)
        assertTrue(productImage.bodyAsText().contains("/uploads/large/"))
        assertTrue(profileImage.bodyAsText().contains("/uploads/large/"))
    }
}

class BadgeApiTest {
    @Test
    fun badge_list_and_admin_grant_revoke_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val list = client.get("/api/v1/badges") { pbHeaders(clientId = "badge-list") }
        val myBadges = client.get("/api/v1/badges/me") { pbHeaders(role = "USER", clientId = "badge-me") }
        val create =
            client.post("/api/v1/admin/badges") {
                pbHeaders(role = "ADMIN", clientId = "badge-create")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """
                    {
                      "name":"구매왕",
                      "description":"주문 50건 이상 달성",
                      "iconUrl":"/badges/purchase-king.svg",
                      "type":"AUTO",
                      "condition":{"metric":"order_count","threshold":50},
                      "rarity":"RARE"
                    }
                    """.trimIndent(),
                )
            }
        val badgeId = Json.parseToJsonElement(create.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val grant =
            client.post("/api/v1/admin/badges/$badgeId/grant") {
                pbHeaders(role = "ADMIN", clientId = "badge-grant")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"userId":5,"reason":"수동 부여"}""")
            }
        val userBadges = client.get("/api/v1/users/5/badges") { pbHeaders(clientId = "badge-user") }
        val revoke = client.delete("/api/v1/admin/badges/$badgeId/revoke/5") { pbHeaders(role = "ADMIN", clientId = "badge-revoke") }

        assertEquals(HttpStatusCode.OK, list.status)
        assertEquals(HttpStatusCode.OK, myBadges.status)
        assertEquals(HttpStatusCode.Created, create.status)
        assertEquals(HttpStatusCode.Created, grant.status)
        assertEquals(HttpStatusCode.OK, userBadges.status)
        assertEquals(HttpStatusCode.OK, revoke.status)
        assertTrue(list.bodyAsText().contains("\"holderCount\""))
        assertTrue(myBadges.bodyAsText().contains("\"badge\""))
        assertTrue(userBadges.bodyAsText().contains("\"구매왕\""))
    }
}
