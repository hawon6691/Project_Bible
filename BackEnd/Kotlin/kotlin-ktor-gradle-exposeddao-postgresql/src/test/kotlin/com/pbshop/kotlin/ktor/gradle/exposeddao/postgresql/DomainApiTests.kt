package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql

import io.ktor.client.request.get
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
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

class AuthApiTest {
    @Test
    fun signup_and_login_routes_follow_the_contract() = testApplication {
        installPbShopApp()

        val signup =
            client.post("/api/v1/auth/signup") {
                pbHeaders(clientId = "auth-signup")
                header("Content-Type", "application/json")
                setBody("""{"email":"fresh-user@nestshop.com","password":"Password1!","name":"새사용자","phone":"010-9999-8888"}""")
            }
        val login =
            client.post("/api/v1/auth/login") {
                pbHeaders(clientId = "auth-login")
                header("Content-Type", "application/json")
                setBody("""{"email":"user1@nestshop.com","password":"Password1!"}""")
            }

        assertEquals(HttpStatusCode.Created, signup.status)
        assertEquals(HttpStatusCode.OK, login.status)
        assertTrue(login.bodyAsText().contains("\"accessToken\""))
        assertTrue(login.bodyAsText().contains("\"requestId\": \"req-test\""))
    }
}

class UserApiTest {
    @Test
    fun current_user_route_requires_user_role_and_returns_profile() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/users/me") { pbHeaders(role = "USER", clientId = "user-me") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"email\": \"user1@nestshop.com\""))
        assertTrue(response.bodyAsText().contains("\"point\": 53000"))
    }

    @Test
    fun user_profile_and_admin_user_routes_follow_the_contract() = testApplication {
        installPbShopApp()

        val profileUpdate =
            client.patch("/api/v1/users/me/profile") {
                pbHeaders(role = "USER", clientId = "user-profile")
                header("Content-Type", "application/json")
                setBody("""{"nickname":"hongpro","bio":"업데이트된 소개"}""")
            }
        val publicProfile = client.get("/api/v1/users/4/profile") { pbHeaders(clientId = "user-public-profile") }
        val adminList = client.get("/api/v1/users?page=1&limit=2&search=user&status=ACTIVE&role=USER") { pbHeaders(role = "ADMIN", clientId = "user-admin-list") }

        assertEquals(HttpStatusCode.OK, profileUpdate.status)
        assertEquals(HttpStatusCode.OK, publicProfile.status)
        assertEquals(HttpStatusCode.OK, adminList.status)
        assertTrue(profileUpdate.bodyAsText().contains("\"nickname\": \"hongpro\""))
        assertTrue(publicProfile.bodyAsText().contains("\"nickname\": \"hongpro\""))
        assertTrue(adminList.bodyAsText().contains("\"totalCount\""))
    }
}

class CategoryApiTest {
    @Test
    fun categories_route_returns_aligned_list_payload() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/categories") { pbHeaders(clientId = "categories") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"컴퓨터\""))
        assertTrue(response.bodyAsText().contains("\"노트북\""))
    }

    @Test
    fun category_detail_and_admin_mutation_routes_follow_the_contract() = testApplication {
        installPbShopApp()

        val detail = client.get("/api/v1/categories/2") { pbHeaders(clientId = "category-detail") }
        val create =
            client.post("/api/v1/categories") {
                pbHeaders(role = "ADMIN", clientId = "category-create")
                header("Content-Type", "application/json")
                setBody("""{"name":"태블릿","sortOrder":3}""")
            }
        val update =
            client.patch("/api/v1/categories/6") {
                pbHeaders(role = "ADMIN", clientId = "category-update")
                header("Content-Type", "application/json")
                setBody("""{"name":"프리미엄 태블릿","sortOrder":4}""")
            }
        val delete = client.delete("/api/v1/categories/6") { pbHeaders(role = "ADMIN", clientId = "category-delete") }

        assertEquals(HttpStatusCode.OK, detail.status)
        assertEquals(HttpStatusCode.Created, create.status)
        assertEquals(HttpStatusCode.OK, update.status)
        assertEquals(HttpStatusCode.OK, delete.status)
        assertTrue(detail.bodyAsText().contains("\"노트북\""))
        assertTrue(create.bodyAsText().contains("\"태블릿\""))
        assertTrue(update.bodyAsText().contains("\"프리미엄 태블릿\""))
        assertTrue(delete.bodyAsText().contains("카테고리가 삭제되었습니다"))
    }
}

class ProductApiTest {
    @Test
    fun product_list_and_detail_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val listResponse = client.get("/api/v1/products?categoryId=2&sort=price_asc&specs={\"CPU\":\"Intel i7-14700H\"}") { pbHeaders(clientId = "products-list") }
        val detailResponse = client.get("/api/v1/products/1") { pbHeaders(clientId = "products-detail") }

        assertEquals(HttpStatusCode.OK, listResponse.status)
        assertEquals(HttpStatusCode.OK, detailResponse.status)
        assertTrue(listResponse.bodyAsText().contains("\"totalPages\""))
        assertTrue(listResponse.bodyAsText().contains("\"게이밍 노트북 A15\""))
        assertTrue(detailResponse.bodyAsText().contains("\"options\""))
        assertTrue(detailResponse.bodyAsText().contains("\"priceEntries\""))
    }

    @Test
    fun admin_product_mutation_option_and_image_routes_follow_the_contract() = testApplication {
        installPbShopApp()

        val create =
            client.post("/api/v1/products") {
                pbHeaders(role = "ADMIN", clientId = "product-create")
                header("Content-Type", "application/json")
                setBody(
                    """
                    {
                      "name":"태블릿 Pro 11",
                      "description":"고해상도 태블릿",
                      "price":850000,
                      "discountPrice":790000,
                      "stock":12,
                      "status":"ON_SALE",
                      "categoryId":2,
                      "thumbnailUrl":"https://img.example.com/tablet-thumb.jpg",
                      "options":[{"name":"색상","values":["블랙","실버"]}],
                      "images":[{"url":"https://img.example.com/tablet-main.jpg","isMain":true,"sortOrder":1}]
                    }
                    """.trimIndent(),
                )
            }
        val createPayload = Json.parseToJsonElement(create.bodyAsText()).jsonObject["data"]!!.jsonObject
        val productId = createPayload["id"]!!.jsonPrimitive.content.toInt()
        val initialOptionId = createPayload["options"]!!.jsonArray.first().jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val update =
            client.patch("/api/v1/products/$productId") {
                pbHeaders(role = "ADMIN", clientId = "product-update")
                header("Content-Type", "application/json")
                setBody("""{"name":"태블릿 Pro 11 2026","stock":15,"options":[{"name":"저장공간","values":["128GB","256GB"]}]}""")
            }
        val updatedPayload = Json.parseToJsonElement(update.bodyAsText()).jsonObject["data"]!!.jsonObject
        val replacedOptionId = updatedPayload["options"]!!.jsonArray.first().jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val addOption =
            client.post("/api/v1/products/$productId/options") {
                pbHeaders(role = "ADMIN", clientId = "product-option-create")
                header("Content-Type", "application/json")
                setBody("""{"name":"보증","values":["기본","2년"]}""")
            }
        val addedOptionId = Json.parseToJsonElement(addOption.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val updateOption =
            client.patch("/api/v1/products/$productId/options/$replacedOptionId") {
                pbHeaders(role = "ADMIN", clientId = "product-option-update")
                header("Content-Type", "application/json")
                setBody("""{"name":"색상","values":["블루","실버"]}""")
            }
        val uploadImage =
            client.post("/api/v1/products/$productId/images") {
                pbHeaders(role = "ADMIN", clientId = "product-image-upload")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("isMain", "true")
                            append("sortOrder", "2")
                            append(
                                "file",
                                "fake-image".encodeToByteArray(),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=tablet-side.jpg")
                                    append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                                },
                            )
                        },
                    ),
                )
            }
        val uploadedImageId = Json.parseToJsonElement(uploadImage.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val deleteImage = client.delete("/api/v1/products/$productId/images/$uploadedImageId") { pbHeaders(role = "ADMIN", clientId = "product-image-delete") }
        val deleteOption = client.delete("/api/v1/products/$productId/options/$addedOptionId") { pbHeaders(role = "ADMIN", clientId = "product-option-delete") }
        val deleteProduct = client.delete("/api/v1/products/$productId") { pbHeaders(role = "ADMIN", clientId = "product-delete") }

        assertEquals(HttpStatusCode.Created, create.status)
        assertEquals(HttpStatusCode.OK, update.status)
        assertEquals(HttpStatusCode.Created, addOption.status)
        assertEquals(HttpStatusCode.OK, updateOption.status)
        assertEquals(HttpStatusCode.Created, uploadImage.status)
        assertEquals(HttpStatusCode.OK, deleteImage.status)
        assertEquals(HttpStatusCode.OK, deleteOption.status)
        assertEquals(HttpStatusCode.OK, deleteProduct.status)
        assertTrue(create.bodyAsText().contains("\"태블릿 Pro 11\""))
        assertTrue(update.bodyAsText().contains("\"태블릿 Pro 11 2026\""))
        assertTrue(addOption.bodyAsText().contains("\"보증\""))
        assertTrue(uploadImage.bodyAsText().contains("tablet-side.jpg"))
        assertTrue(initialOptionId > 0)
    }
}

class SpecSellerPriceApiTest {
    @Test
    fun spec_seller_and_price_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val specResponse = client.get("/api/v1/specs/definitions") { pbHeaders(clientId = "specs") }
        val specCompare =
            client.post("/api/v1/specs/compare") {
                pbHeaders(clientId = "specs-compare")
                header("Content-Type", "application/json")
                setBody("""{"productIds":[1,2]}""")
            }
        val specReplace =
            client.put("/api/v1/products/1/specs") {
                pbHeaders(role = "ADMIN", clientId = "product-specs-replace")
                header("Content-Type", "application/json")
                setBody("""[{"specDefinitionId":1,"value":"Intel Ultra 9"},{"specDefinitionId":2,"value":"32","numericValue":32.0}]""")
            }
        val sellerResponse = client.get("/api/v1/sellers?page=1&limit=2") { pbHeaders(clientId = "sellers") }
        val sellerCreate =
            client.post("/api/v1/sellers") {
                pbHeaders(role = "ADMIN", clientId = "seller-create")
                header("Content-Type", "application/json")
                setBody("""{"name":"딜마트","url":"https://dealmart.example.com","trustScore":88,"isActive":true}""")
            }
        val sellerId = Json.parseToJsonElement(sellerCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val sellerUpdate =
            client.patch("/api/v1/sellers/$sellerId") {
                pbHeaders(role = "ADMIN", clientId = "seller-update")
                header("Content-Type", "application/json")
                setBody("""{"description":"신규 판매처","trustGrade":"A"}""")
            }
        val sellerDetail = client.get("/api/v1/sellers/$sellerId") { pbHeaders(clientId = "seller-detail") }
        val priceResponse = client.get("/api/v1/products/1/prices") { pbHeaders(clientId = "prices") }
        val priceCreate =
            client.post("/api/v1/products/1/prices") {
                pbHeaders(role = "SELLER", clientId = "price-create")
                header("Content-Type", "application/json")
                setBody("""{"sellerId":3,"price":1690000,"shippingInfo":"무료배송","productUrl":"https://carworld.example.com/p/1","shippingFee":0,"shippingType":"FREE"}""")
            }
        val priceId = Json.parseToJsonElement(priceCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val priceUpdate =
            client.patch("/api/v1/prices/$priceId") {
                pbHeaders(role = "SELLER", clientId = "price-update")
                header("Content-Type", "application/json")
                setBody("""{"price":1680000,"shippingInfo":"특급배송"}""")
            }
        val priceHistory = client.get("/api/v1/products/1/price-history") { pbHeaders(clientId = "price-history") }
        val alertCreate =
            client.post("/api/v1/price-alerts") {
                pbHeaders(role = "USER", clientId = "price-alert-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"productId":1,"targetPrice":1650000,"isActive":true}""")
            }
        val alertList =
            client.get("/api/v1/price-alerts?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "price-alert-list")
                header("X-User-Id", "4")
            }
        val priceDelete = client.delete("/api/v1/prices/$priceId") { pbHeaders(role = "ADMIN", clientId = "price-delete") }

        assertEquals(HttpStatusCode.OK, specResponse.status)
        assertEquals(HttpStatusCode.OK, specCompare.status)
        assertEquals(HttpStatusCode.OK, specReplace.status)
        assertEquals(HttpStatusCode.OK, sellerResponse.status)
        assertEquals(HttpStatusCode.Created, sellerCreate.status)
        assertEquals(HttpStatusCode.OK, sellerUpdate.status)
        assertEquals(HttpStatusCode.OK, sellerDetail.status)
        assertEquals(HttpStatusCode.OK, priceResponse.status)
        assertEquals(HttpStatusCode.Created, priceCreate.status)
        assertEquals(HttpStatusCode.OK, priceUpdate.status)
        assertEquals(HttpStatusCode.OK, priceHistory.status)
        assertEquals(HttpStatusCode.Created, alertCreate.status)
        assertEquals(HttpStatusCode.OK, alertList.status)
        assertEquals(HttpStatusCode.OK, priceDelete.status)
        assertTrue(specCompare.bodyAsText().contains("\"diff\""))
        assertTrue(specReplace.bodyAsText().contains("Intel Ultra 9"))
        assertTrue(sellerDetail.bodyAsText().contains("딜마트"))
        assertTrue(priceUpdate.bodyAsText().contains("1680000"))
        assertTrue(alertList.bodyAsText().contains("\"targetPrice\""))
    }
}

class CartAddressApiTest {
    @Test
    fun cart_and_address_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val cartList =
            client.get("/api/v1/cart") {
                pbHeaders(role = "USER", clientId = "cart-list")
                header("X-User-Id", "4")
            }
        val cartCreate =
            client.post("/api/v1/cart") {
                pbHeaders(role = "USER", clientId = "cart-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"productId":3,"sellerId":1,"quantity":2,"selectedOptions":"기본"}""")
            }
        val createdCartItemId = Json.parseToJsonElement(cartCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val cartUpdate =
            client.patch("/api/v1/cart/$createdCartItemId") {
                pbHeaders(role = "USER", clientId = "cart-update")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"quantity":3}""")
            }
        val cartDelete =
            client.delete("/api/v1/cart/$createdCartItemId") {
                pbHeaders(role = "USER", clientId = "cart-delete")
                header("X-User-Id", "4")
            }
        val cartClear =
            client.delete("/api/v1/cart") {
                pbHeaders(role = "USER", clientId = "cart-clear")
                header("X-User-Id", "4")
            }

        val addressList =
            client.get("/api/v1/addresses") {
                pbHeaders(role = "USER", clientId = "address-list")
                header("X-User-Id", "4")
            }
        val addressCreate =
            client.post("/api/v1/addresses") {
                pbHeaders(role = "USER", clientId = "address-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"label":"부모님댁","recipientName":"홍길동","phone":"01099998888","zipCode":"13579","address":"서울시 송파구 올림픽로 300","addressDetail":"35층","isDefault":false}""")
            }
        val createdAddressId = Json.parseToJsonElement(addressCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val addressUpdate =
            client.patch("/api/v1/addresses/$createdAddressId") {
                pbHeaders(role = "USER", clientId = "address-update")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"label":"새 배송지","isDefault":true}""")
            }
        val addressDelete =
            client.delete("/api/v1/addresses/$createdAddressId") {
                pbHeaders(role = "USER", clientId = "address-delete")
                header("X-User-Id", "4")
            }

        assertEquals(HttpStatusCode.OK, cartList.status)
        assertEquals(HttpStatusCode.Created, cartCreate.status)
        assertEquals(HttpStatusCode.OK, cartUpdate.status)
        assertEquals(HttpStatusCode.OK, cartDelete.status)
        assertEquals(HttpStatusCode.OK, cartClear.status)
        assertEquals(HttpStatusCode.OK, addressList.status)
        assertEquals(HttpStatusCode.Created, addressCreate.status)
        assertEquals(HttpStatusCode.OK, addressUpdate.status)
        assertEquals(HttpStatusCode.OK, addressDelete.status)
        assertTrue(cartList.bodyAsText().contains("\"게이밍 노트북 A15\""))
        assertTrue(cartUpdate.bodyAsText().contains("\"quantity\": 3"))
        assertTrue(addressList.bodyAsText().contains("\"recipientName\": \"홍길동\""))
        assertTrue(addressUpdate.bodyAsText().contains("\"isDefault\": true"))
    }
}

class OrderPaymentApiTest {
    @Test
    fun order_and_payment_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val orderCreate =
            client.post("/api/v1/orders") {
                pbHeaders(role = "USER", clientId = "order-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody(
                    """
                    {
                      "addressId": 1,
                      "items": [
                        { "productId": 1, "sellerId": 1, "quantity": 1, "selectedOptions": "RAM:16GB,SSD:1TB" }
                      ],
                      "fromCart": false,
                      "usePoint": 5000,
                      "memo": "부재 시 문 앞"
                    }
                    """.trimIndent(),
                )
            }
        val createdOrderId = Json.parseToJsonElement(orderCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val orderList =
            client.get("/api/v1/orders?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "order-list")
                header("X-User-Id", "4")
            }
        val orderDetail =
            client.get("/api/v1/orders/$createdOrderId") {
                pbHeaders(role = "USER", clientId = "order-detail")
                header("X-User-Id", "4")
            }
        val adminList =
            client.get("/api/v1/admin/orders?page=1&limit=10") {
                pbHeaders(role = "ADMIN", clientId = "order-admin-list")
            }
        val adminUpdate =
            client.patch("/api/v1/admin/orders/$createdOrderId/status") {
                pbHeaders(role = "ADMIN", clientId = "order-admin-update")
                header("Content-Type", "application/json")
                setBody("""{"status":"SHIPPING"}""")
            }
        val paymentCreate =
            client.post("/api/v1/payments") {
                pbHeaders(role = "USER", clientId = "payment-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"orderId":$createdOrderId,"method":"CARD"}""")
            }
        val paymentId = Json.parseToJsonElement(paymentCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val paymentDetail =
            client.get("/api/v1/payments/$paymentId") {
                pbHeaders(role = "USER", clientId = "payment-detail")
                header("X-User-Id", "4")
            }
        val paymentRefund =
            client.post("/api/v1/payments/$paymentId/refund") {
                pbHeaders(role = "USER", clientId = "payment-refund")
                header("X-User-Id", "4")
            }
        val orderCancel =
            client.post("/api/v1/orders/$createdOrderId/cancel") {
                pbHeaders(role = "USER", clientId = "order-cancel")
                header("X-User-Id", "4")
            }

        assertEquals(HttpStatusCode.Created, orderCreate.status)
        assertEquals(HttpStatusCode.OK, orderList.status)
        assertEquals(HttpStatusCode.OK, orderDetail.status)
        assertEquals(HttpStatusCode.OK, adminList.status)
        assertEquals(HttpStatusCode.OK, adminUpdate.status)
        assertEquals(HttpStatusCode.Created, paymentCreate.status)
        assertEquals(HttpStatusCode.OK, paymentDetail.status)
        assertEquals(HttpStatusCode.OK, paymentRefund.status)
        assertEquals(HttpStatusCode.OK, orderCancel.status)
        assertTrue(orderCreate.bodyAsText().contains("\"ORDER_PLACED\""))
        assertTrue(orderList.bodyAsText().contains("\"totalCount\""))
        assertTrue(orderDetail.bodyAsText().contains("\"shippingAddress\""))
        assertTrue(adminUpdate.bodyAsText().contains("\"SHIPPING\""))
        assertTrue(paymentCreate.bodyAsText().contains("\"COMPLETED\""))
        assertTrue(paymentRefund.bodyAsText().contains("\"REFUNDED\""))
        assertTrue(orderCancel.bodyAsText().contains("\"CANCELLED\""))
    }
}

class ReviewWishlistPointApiTest {
    @Test
    fun review_wishlist_and_point_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val orderCreate =
            client.post("/api/v1/orders") {
                pbHeaders(role = "USER", clientId = "review-order-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody(
                    """
                    {
                      "addressId": 1,
                      "items": [
                        { "productId": 3, "sellerId": 1, "quantity": 1, "selectedOptions": "기본" }
                      ],
                      "fromCart": false,
                      "usePoint": 0,
                      "memo": "리뷰 테스트 주문"
                    }
                    """.trimIndent(),
                )
            }
        val orderId = Json.parseToJsonElement(orderCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val reviewCreate =
            client.post("/api/v1/products/3/reviews") {
                pbHeaders(role = "USER", clientId = "review-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"orderId":$orderId,"rating":5,"content":"화면이 정말 선명하고 가볍습니다!"}""")
            }
        val reviewId = Json.parseToJsonElement(reviewCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val reviewList = client.get("/api/v1/products/3/reviews?page=1&limit=10") { pbHeaders(clientId = "review-list") }
        val reviewUpdate =
            client.patch("/api/v1/reviews/$reviewId") {
                pbHeaders(role = "USER", clientId = "review-update")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"rating":4,"content":"수정된 리뷰입니다."}""")
            }
        val wishlistList =
            client.get("/api/v1/wishlist?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "wishlist-list")
                header("X-User-Id", "4")
            }
        val wishlistToggleOn =
            client.post("/api/v1/wishlist/3") {
                pbHeaders(role = "USER", clientId = "wishlist-toggle-on")
                header("X-User-Id", "4")
            }
        val wishlistToggleOff =
            client.post("/api/v1/wishlist/3") {
                pbHeaders(role = "USER", clientId = "wishlist-toggle-off")
                header("X-User-Id", "4")
            }
        val wishlistDelete =
            client.delete("/api/v1/wishlist/2") {
                pbHeaders(role = "USER", clientId = "wishlist-delete")
                header("X-User-Id", "4")
            }
        val pointBalance =
            client.get("/api/v1/points/balance") {
                pbHeaders(role = "USER", clientId = "points-balance")
                header("X-User-Id", "4")
            }
        val pointTransactions =
            client.get("/api/v1/points/transactions?page=1&limit=10&type=USE") {
                pbHeaders(role = "USER", clientId = "points-transactions")
                header("X-User-Id", "4")
            }
        val pointGrant =
            client.post("/api/v1/admin/points/grant") {
                pbHeaders(role = "ADMIN", clientId = "points-grant")
                header("Content-Type", "application/json")
                setBody("""{"userId":4,"amount":1000,"description":"이벤트 지급"}""")
            }
        val reviewDelete =
            client.delete("/api/v1/reviews/$reviewId") {
                pbHeaders(role = "USER", clientId = "review-delete")
                header("X-User-Id", "4")
            }

        assertEquals(HttpStatusCode.Created, orderCreate.status)
        assertEquals(HttpStatusCode.Created, reviewCreate.status)
        assertEquals(HttpStatusCode.OK, reviewList.status)
        assertEquals(HttpStatusCode.OK, reviewUpdate.status)
        assertEquals(HttpStatusCode.OK, wishlistList.status)
        assertEquals(HttpStatusCode.OK, wishlistToggleOn.status)
        assertEquals(HttpStatusCode.OK, wishlistToggleOff.status)
        assertEquals(HttpStatusCode.OK, wishlistDelete.status)
        assertEquals(HttpStatusCode.OK, pointBalance.status)
        assertEquals(HttpStatusCode.OK, pointTransactions.status)
        assertEquals(HttpStatusCode.Created, pointGrant.status)
        assertEquals(HttpStatusCode.OK, reviewDelete.status)
        assertTrue(reviewCreate.bodyAsText().contains("\"awardedPoint\": 500"))
        assertTrue(reviewList.bodyAsText().contains("\"rating\""))
        assertTrue(reviewUpdate.bodyAsText().contains("수정된 리뷰입니다"))
        assertTrue(wishlistList.bodyAsText().contains("\"productId\""))
        assertTrue(wishlistToggleOn.bodyAsText().contains("\"wishlisted\": true"))
        assertTrue(wishlistToggleOff.bodyAsText().contains("\"wishlisted\": false"))
        assertTrue(pointBalance.bodyAsText().contains("\"balance\""))
        assertTrue(pointTransactions.bodyAsText().contains("\"type\": \"USE\""))
        assertTrue(pointGrant.bodyAsText().contains("\"ADMIN_GRANT\""))
    }
}

class CommunityInquirySupportApiTest {
    @Test
    fun community_inquiry_and_support_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val boardList = client.get("/api/v1/boards") { pbHeaders(clientId = "community-boards") }
        val postList = client.get("/api/v1/boards/1/posts?page=1&limit=10&sort=newest") { pbHeaders(clientId = "community-post-list") }
        val postCreate =
            client.post("/api/v1/boards/1/posts") {
                pbHeaders(role = "USER", clientId = "community-post-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"title":"새 커뮤니티 글","content":"커뮤니티 실구현 테스트 본문입니다."}""")
            }
        val postId = Json.parseToJsonElement(postCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val postUpdate =
            client.patch("/api/v1/posts/$postId") {
                pbHeaders(role = "USER", clientId = "community-post-update")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"title":"수정된 커뮤니티 글"}""")
            }
        val likeToggle =
            client.post("/api/v1/posts/$postId/like") {
                pbHeaders(role = "USER", clientId = "community-like")
                header("X-User-Id", "4")
            }
        val commentCreate =
            client.post("/api/v1/posts/$postId/comments") {
                pbHeaders(role = "USER", clientId = "community-comment-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"content":"좋은 글 감사합니다."}""")
            }
        val commentId = Json.parseToJsonElement(commentCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val commentList = client.get("/api/v1/posts/$postId/comments") { pbHeaders(clientId = "community-comment-list") }
        val commentDelete =
            client.delete("/api/v1/comments/$commentId") {
                pbHeaders(role = "USER", clientId = "community-comment-delete")
                header("X-User-Id", "4")
            }
        val postDetail = client.get("/api/v1/posts/$postId") { pbHeaders(clientId = "community-post-detail") }
        val postDelete =
            client.delete("/api/v1/posts/$postId") {
                pbHeaders(role = "USER", clientId = "community-post-delete")
                header("X-User-Id", "4")
            }

        val inquiryCreate =
            client.post("/api/v1/products/1/inquiries") {
                pbHeaders(role = "USER", clientId = "inquiry-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"title":"확장 슬롯 문의","content":"추가 SSD 슬롯이 있나요?","isSecret":false}""")
            }
        val inquiryId = Json.parseToJsonElement(inquiryCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val inquiryList = client.get("/api/v1/products/1/inquiries?page=1&limit=10") { pbHeaders(clientId = "inquiry-list") }
        val inquiryMine =
            client.get("/api/v1/inquiries/me?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "inquiry-my")
                header("X-User-Id", "4")
            }
        val inquiryAnswer =
            client.post("/api/v1/inquiries/$inquiryId/answer") {
                pbHeaders(role = "ADMIN", clientId = "inquiry-answer")
                header("Content-Type", "application/json")
                setBody("""{"answer":"네, 하단 슬롯 1개를 추가로 지원합니다."}""")
            }
        val inquiryDelete =
            client.delete("/api/v1/inquiries/$inquiryId") {
                pbHeaders(role = "USER", clientId = "inquiry-delete")
                header("X-User-Id", "4")
            }

        val supportCreate =
            client.post("/api/v1/support/tickets") {
                pbHeaders(role = "USER", clientId = "support-create")
                header("X-User-Id", "4")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("category", "ORDER")
                            append("title", "주문 상태 문의")
                            append("content", "주문한 상품의 출고 일정을 알고 싶습니다.")
                            append(
                                "attachments",
                                "fake-ticket-image".encodeToByteArray(),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=ticket-proof.png")
                                    append(HttpHeaders.ContentType, ContentType.Image.PNG.toString())
                                },
                            )
                        },
                    ),
                )
            }
        val ticketId = Json.parseToJsonElement(supportCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val supportList =
            client.get("/api/v1/support/tickets?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "support-list")
                header("X-User-Id", "4")
            }
        val supportDetail =
            client.get("/api/v1/support/tickets/$ticketId") {
                pbHeaders(role = "USER", clientId = "support-detail")
                header("X-User-Id", "4")
            }
        val supportReply =
            client.post("/api/v1/support/tickets/$ticketId/reply") {
                pbHeaders(role = "ADMIN", clientId = "support-reply")
                header("X-User-Id", "1")
                header("Content-Type", "application/json")
                setBody("""{"content":"내일 오전에 출고될 예정입니다."}""")
            }
        val supportAdminList = client.get("/api/v1/admin/support/tickets?page=1&limit=10&status=OPEN") { pbHeaders(role = "ADMIN", clientId = "support-admin-list") }
        val supportStatus =
            client.patch("/api/v1/admin/support/tickets/$ticketId/status") {
                pbHeaders(role = "ADMIN", clientId = "support-status")
                header("Content-Type", "application/json")
                setBody("""{"status":"RESOLVED"}""")
            }

        val faqList = client.get("/api/v1/faqs?category=ORDER&search=주문") { pbHeaders(clientId = "faq-list") }
        val faqCreate =
            client.post("/api/v1/faqs") {
                pbHeaders(role = "ADMIN", clientId = "faq-create")
                header("Content-Type", "application/json")
                setBody("""{"category":"ACCOUNT","question":"휴면 계정은 언제 해제되나요?","answer":"로그인 후 즉시 해제됩니다.","sortOrder":3,"isActive":true}""")
            }
        val faqId = Json.parseToJsonElement(faqCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val faqUpdate =
            client.patch("/api/v1/faqs/$faqId") {
                pbHeaders(role = "ADMIN", clientId = "faq-update")
                header("Content-Type", "application/json")
                setBody("""{"answer":"본인 인증 후 즉시 해제됩니다.","isActive":true}""")
            }
        val faqDelete = client.delete("/api/v1/faqs/$faqId") { pbHeaders(role = "ADMIN", clientId = "faq-delete") }

        val noticeList = client.get("/api/v1/notices?page=1&limit=10") { pbHeaders(clientId = "notice-list") }
        val noticeCreate =
            client.post("/api/v1/notices") {
                pbHeaders(role = "ADMIN", clientId = "notice-create")
                header("Content-Type", "application/json")
                setBody("""{"title":"신규 이벤트 안내","content":"봄맞이 할인 이벤트가 시작됩니다.","isPinned":true}""")
            }
        val noticeId = Json.parseToJsonElement(noticeCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val noticeDetail = client.get("/api/v1/notices/$noticeId") { pbHeaders(clientId = "notice-detail") }
        val noticeUpdate =
            client.patch("/api/v1/notices/$noticeId") {
                pbHeaders(role = "ADMIN", clientId = "notice-update")
                header("Content-Type", "application/json")
                setBody("""{"title":"수정된 이벤트 안내","isPinned":false}""")
            }
        val noticeDelete = client.delete("/api/v1/notices/$noticeId") { pbHeaders(role = "ADMIN", clientId = "notice-delete") }

        assertEquals(HttpStatusCode.OK, boardList.status)
        assertEquals(HttpStatusCode.OK, postList.status)
        assertEquals(HttpStatusCode.Created, postCreate.status)
        assertEquals(HttpStatusCode.OK, postUpdate.status)
        assertEquals(HttpStatusCode.OK, likeToggle.status)
        assertEquals(HttpStatusCode.Created, commentCreate.status)
        assertEquals(HttpStatusCode.OK, commentList.status)
        assertEquals(HttpStatusCode.OK, commentDelete.status)
        assertEquals(HttpStatusCode.OK, postDetail.status)
        assertEquals(HttpStatusCode.OK, postDelete.status)
        assertEquals(HttpStatusCode.Created, inquiryCreate.status)
        assertEquals(HttpStatusCode.OK, inquiryList.status)
        assertEquals(HttpStatusCode.OK, inquiryMine.status)
        assertEquals(HttpStatusCode.OK, inquiryAnswer.status)
        assertEquals(HttpStatusCode.OK, inquiryDelete.status)
        assertEquals(HttpStatusCode.Created, supportCreate.status)
        assertEquals(HttpStatusCode.OK, supportList.status)
        assertEquals(HttpStatusCode.OK, supportDetail.status)
        assertEquals(HttpStatusCode.Created, supportReply.status)
        assertEquals(HttpStatusCode.OK, supportAdminList.status)
        assertEquals(HttpStatusCode.OK, supportStatus.status)
        assertEquals(HttpStatusCode.OK, faqList.status)
        assertEquals(HttpStatusCode.Created, faqCreate.status)
        assertEquals(HttpStatusCode.OK, faqUpdate.status)
        assertEquals(HttpStatusCode.OK, faqDelete.status)
        assertEquals(HttpStatusCode.OK, noticeList.status)
        assertEquals(HttpStatusCode.Created, noticeCreate.status)
        assertEquals(HttpStatusCode.OK, noticeDetail.status)
        assertEquals(HttpStatusCode.OK, noticeUpdate.status)
        assertEquals(HttpStatusCode.OK, noticeDelete.status)
        assertTrue(boardList.bodyAsText().contains("\"자유게시판\""))
        assertTrue(postUpdate.bodyAsText().contains("수정된 커뮤니티 글"))
        assertTrue(likeToggle.bodyAsText().contains("\"liked\""))
        assertTrue(inquiryAnswer.bodyAsText().contains("\"status\": \"ANSWERED\""))
        assertTrue(supportCreate.bodyAsText().contains("\"ticketNumber\""))
        assertTrue(supportDetail.bodyAsText().contains("\"replies\""))
        assertTrue(supportStatus.bodyAsText().contains("\"RESOLVED\""))
        assertTrue(faqUpdate.bodyAsText().contains("본인 인증 후 즉시 해제됩니다"))
        assertTrue(noticeDetail.bodyAsText().contains("\"viewCount\""))
        assertTrue(noticeUpdate.bodyAsText().contains("수정된 이벤트 안내"))
    }
}

class ActivityChatPushApiTest {
    @Test
    fun activity_chat_and_push_routes_follow_the_actual_contract() = testApplication {
        installPbShopApp()

        val viewList =
            client.get("/api/v1/activity/views?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "activity-views")
                header("X-User-Id", "4")
            }
        val searchList =
            client.get("/api/v1/activity/searches?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "activity-searches")
                header("X-User-Id", "4")
            }
        val deleteSearch =
            client.delete("/api/v1/activity/searches/1") {
                pbHeaders(role = "USER", clientId = "activity-search-delete")
                header("X-User-Id", "4")
            }
        val clearSearches =
            client.delete("/api/v1/activity/searches") {
                pbHeaders(role = "USER", clientId = "activity-search-clear")
                header("X-User-Id", "4")
            }
        val clearViews =
            client.delete("/api/v1/activity/views") {
                pbHeaders(role = "USER", clientId = "activity-view-clear")
                header("X-User-Id", "4")
            }

        val chatCreate =
            client.post("/api/v1/chat/rooms") {
                pbHeaders(role = "USER", clientId = "chat-create")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"name":"배송 문의 신규 방","isPrivate":true}""")
            }
        val createdRoomId = Json.parseToJsonElement(chatCreate.bodyAsText()).jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content.toInt()
        val chatList =
            client.get("/api/v1/chat/rooms?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "chat-list")
                header("X-User-Id", "4")
            }
        val adminChatList = client.get("/api/v1/chat/rooms?page=1&limit=10") { pbHeaders(role = "ADMIN", clientId = "chat-admin-list") }
        val chatMessages =
            client.get("/api/v1/chat/rooms/1/messages?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "chat-messages")
                header("X-User-Id", "4")
            }
        val forbiddenMessages =
            client.get("/api/v1/chat/rooms/1/messages?page=1&limit=10") {
                pbHeaders(role = "USER", clientId = "chat-messages-forbidden")
                header("X-User-Id", "5")
            }
        val closeRoom =
            client.patch("/api/v1/chat/rooms/$createdRoomId/close") {
                pbHeaders(role = "USER", clientId = "chat-close")
                header("X-User-Id", "4")
            }

        val subscribe =
            client.post("/api/v1/push/subscriptions") {
                pbHeaders(role = "USER", clientId = "push-subscribe")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"endpoint":"https://push.example.com/sub/new-1","p256dhKey":"k1","authKey":"a1","expirationTime":1741000000000}""")
            }
        val subscriptionList =
            client.get("/api/v1/push/subscriptions") {
                pbHeaders(role = "USER", clientId = "push-list")
                header("X-User-Id", "4")
            }
        val preferenceGet =
            client.get("/api/v1/push/preferences") {
                pbHeaders(role = "USER", clientId = "push-pref-get")
                header("X-User-Id", "4")
            }
        val preferenceUpdate =
            client.post("/api/v1/push/preferences") {
                pbHeaders(role = "USER", clientId = "push-pref-update")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"priceAlertEnabled":false,"orderStatusEnabled":true,"chatMessageEnabled":false,"dealEnabled":true}""")
            }
        val unsubscribe =
            client.post("/api/v1/push/subscriptions/unsubscribe") {
                pbHeaders(role = "USER", clientId = "push-unsubscribe")
                header("X-User-Id", "4")
                header("Content-Type", "application/json")
                setBody("""{"endpoint":"https://push.example.com/sub/new-1"}""")
            }

        assertEquals(HttpStatusCode.OK, viewList.status)
        assertEquals(HttpStatusCode.OK, searchList.status)
        assertEquals(HttpStatusCode.OK, deleteSearch.status)
        assertEquals(HttpStatusCode.OK, clearSearches.status)
        assertEquals(HttpStatusCode.OK, clearViews.status)
        assertEquals(HttpStatusCode.Created, chatCreate.status)
        assertEquals(HttpStatusCode.OK, chatList.status)
        assertEquals(HttpStatusCode.OK, adminChatList.status)
        assertEquals(HttpStatusCode.OK, chatMessages.status)
        assertEquals(HttpStatusCode.Forbidden, forbiddenMessages.status)
        assertEquals(HttpStatusCode.OK, closeRoom.status)
        assertEquals(HttpStatusCode.Created, subscribe.status)
        assertEquals(HttpStatusCode.OK, subscriptionList.status)
        assertEquals(HttpStatusCode.OK, preferenceGet.status)
        assertEquals(HttpStatusCode.OK, preferenceUpdate.status)
        assertEquals(HttpStatusCode.OK, unsubscribe.status)
        assertTrue(viewList.bodyAsText().contains("\"productName\""))
        assertTrue(searchList.bodyAsText().contains("\"keyword\""))
        assertTrue(chatList.bodyAsText().contains("\"배송 문의 방\""))
        assertTrue(adminChatList.bodyAsText().contains("\"status\""))
        assertTrue(chatMessages.bodyAsText().contains("배송 관련 문의드립니다"))
        assertTrue(closeRoom.bodyAsText().contains("\"status\": \"CLOSED\""))
        assertTrue(subscribe.bodyAsText().contains("https://push.example.com/sub/new-1"))
        assertTrue(subscriptionList.bodyAsText().contains("\"isActive\": true"))
        assertTrue(preferenceGet.bodyAsText().contains("\"priceAlertEnabled\""))
        assertTrue(preferenceUpdate.bodyAsText().contains("\"chatMessageEnabled\": false"))
        assertTrue(unsubscribe.bodyAsText().contains("\"success\": true"))
    }
}

class PredictionDealRecommendationRankingApiTest {
    @Test
    fun prediction_deal_recommendation_and_ranking_routes_work() = testApplication {
        installPbShopApp()

        val predictionResponse = client.get("/api/v1/predictions/products/1/price-trend") { pbHeaders(clientId = "prediction") }
        val dealResponse = client.get("/api/v1/deals") { pbHeaders(clientId = "deals") }
        val recommendationResponse = client.get("/api/v1/recommendations/today") { pbHeaders(clientId = "recommendation") }
        val rankingResponse = client.get("/api/v1/rankings/products/popular") { pbHeaders(clientId = "ranking") }

        assertEquals(HttpStatusCode.OK, predictionResponse.status)
        assertEquals(HttpStatusCode.OK, dealResponse.status)
        assertEquals(HttpStatusCode.OK, recommendationResponse.status)
        assertEquals(HttpStatusCode.OK, rankingResponse.status)
    }
}

class FraudTrustI18nImageBadgeApiTest {
    @Test
    fun fraud_trust_i18n_image_and_badge_routes_work() = testApplication {
        installPbShopApp()

        val fraudResponse = client.get("/api/v1/fraud/alerts") { pbHeaders(role = "ADMIN", clientId = "fraud") }
        val trustResponse = client.get("/api/v1/sellers/1/trust") { pbHeaders(clientId = "trust") }
        val i18nResponse = client.get("/api/v1/i18n/translations") { pbHeaders(clientId = "i18n") }
        val badgeResponse = client.get("/api/v1/badges") { pbHeaders(clientId = "badges") }

        assertEquals(HttpStatusCode.OK, fraudResponse.status)
        assertEquals(HttpStatusCode.OK, trustResponse.status)
        assertEquals(HttpStatusCode.OK, i18nResponse.status)
        assertEquals(HttpStatusCode.OK, badgeResponse.status)
    }
}

class PcFriendShortformMediaNewsMatchingApiTest {
    @Test
    fun pc_friend_shortform_media_news_and_matching_routes_work() = testApplication {
        installPbShopApp()

        val pcResponse = client.get("/api/v1/pc-builds") { pbHeaders(role = "USER", clientId = "pc") }
        val friendResponse = client.get("/api/v1/friends") { pbHeaders(role = "USER", clientId = "friends") }
        val shortformResponse = client.get("/api/v1/shortforms") { pbHeaders(clientId = "shortforms") }
        val mediaResponse = client.post("/api/v1/media/presigned-url") { pbHeaders(role = "USER", clientId = "media") }
        val newsResponse = client.get("/api/v1/news") { pbHeaders(clientId = "news") }
        val matchingResponse = client.get("/api/v1/matching/pending") { pbHeaders(role = "ADMIN", clientId = "matching") }

        assertEquals(HttpStatusCode.OK, pcResponse.status)
        assertEquals(HttpStatusCode.OK, friendResponse.status)
        assertEquals(HttpStatusCode.OK, shortformResponse.status)
        assertEquals(HttpStatusCode.OK, mediaResponse.status)
        assertEquals(HttpStatusCode.OK, newsResponse.status)
        assertEquals(HttpStatusCode.OK, matchingResponse.status)
    }
}

class AnalyticsUsedMarketAutoAuctionCompareApiTest {
    @Test
    fun analytics_used_market_auto_auction_and_compare_routes_work() = testApplication {
        installPbShopApp()

        val analyticsResponse = client.get("/api/v1/analytics/products/1/lowest-ever") { pbHeaders(clientId = "analytics") }
        val usedMarketResponse = client.get("/api/v1/used-market/products/1/price") { pbHeaders(clientId = "used-market") }
        val autoResponse = client.get("/api/v1/auto/models") { pbHeaders(clientId = "auto") }
        val auctionResponse = client.get("/api/v1/auctions") { pbHeaders(clientId = "auction") }
        val compareResponse = client.get("/api/v1/compare") { pbHeaders(clientId = "compare") }

        assertEquals(HttpStatusCode.OK, analyticsResponse.status)
        assertEquals(HttpStatusCode.OK, usedMarketResponse.status)
        assertEquals(HttpStatusCode.OK, autoResponse.status)
        assertEquals(HttpStatusCode.OK, auctionResponse.status)
        assertEquals(HttpStatusCode.OK, compareResponse.status)
    }
}

class HealthRouteTest {
    @Test
    fun health_route_reflects_db_status_and_component_checks() = testApplication {
        installPbShopApp()

        val response = client.get("/api/v1/health") { pbHeaders(clientId = "health") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("kotlin-ktor-gradle-exposeddao-postgresql"))
    }
}

class OpsApiTest {
    @Test
    fun ops_routes_cover_query_resilience_queue_and_observability() = testApplication {
        installPbShopApp()

        val queryResponse = client.get("/api/v1/query/products") { pbHeaders(clientId = "query") }
        val resilienceResponse = client.get("/api/v1/resilience/circuit-breakers") { pbHeaders(role = "ADMIN", clientId = "resilience") }
        val queueResponse = client.get("/api/v1/admin/queues/stats") { pbHeaders(role = "ADMIN", clientId = "queue") }
        val observabilityResponse = client.get("/api/v1/admin/observability/dashboard") { pbHeaders(role = "ADMIN", clientId = "observability") }

        assertEquals(HttpStatusCode.OK, queryResponse.status)
        assertEquals(HttpStatusCode.OK, resilienceResponse.status)
        assertEquals(HttpStatusCode.OK, queueResponse.status)
        assertEquals(HttpStatusCode.OK, observabilityResponse.status)
    }
}
