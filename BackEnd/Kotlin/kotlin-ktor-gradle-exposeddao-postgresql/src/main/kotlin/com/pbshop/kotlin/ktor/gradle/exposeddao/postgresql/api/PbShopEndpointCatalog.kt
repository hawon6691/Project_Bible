package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall

fun pbShopEndpoints(): List<EndpointDefinition> =
    authEndpoints() +
        userEndpoints() +
        catalogEndpoints() +
        commerceEndpoints() +
        engagementEndpoints() +
        discoveryEndpoints() +
        mediaEndpoints() +
        builderEndpoints() +
        opsEndpoints()

private fun authEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Post, "/auth/signup", "Auth", "Sign up") {
            StubResponse(
                status = HttpStatusCode.Created,
                data = mapOf("id" to 101, "email" to "user@pbshop.dev", "status" to "PENDING_VERIFICATION"),
            )
        },
        endpoint(HttpMethod.Post, "/auth/verify-email", "Auth", "Verify email") { message("Email verification completed.") },
        endpoint(HttpMethod.Post, "/auth/resend-verification", "Auth", "Resend verification") { message("Verification email resent.") },
        endpoint(HttpMethod.Post, "/auth/login", "Auth", "Login") {
            StubResponse(
                data =
                    mapOf(
                        "accessToken" to "pbshop-access-token",
                        "refreshToken" to "pbshop-refresh-token",
                        "user" to mapOf("id" to 1, "email" to "user@pbshop.dev", "role" to "USER"),
                    ),
            )
        },
        endpoint(HttpMethod.Post, "/auth/password-reset/request", "Auth", "Request password reset") { message("Password reset request accepted.") },
        endpoint(HttpMethod.Post, "/auth/password-reset/verify", "Auth", "Verify password reset token") { message("Password reset token is valid.") },
        endpoint(HttpMethod.Post, "/auth/password-reset/confirm", "Auth", "Confirm password reset") { message("Password reset completed.") },
        endpoint(HttpMethod.Get, "/auth/login/{provider}", "Auth", "Social login redirect") { call ->
            StubResponse(data = mapOf("provider" to call.pathParam("provider", "google"), "redirectUrl" to "https://auth.pbshop.dev/oauth/start"))
        },
        endpoint(HttpMethod.Get, "/auth/callback/{provider}", "Auth", "Social login callback") { call ->
            StubResponse(data = mapOf("provider" to call.pathParam("provider", "google"), "linked" to true))
        },
        endpoint(HttpMethod.Post, "/auth/social/link", "Auth", "Link social account", roles = setOf(PbRole.USER)) { message("Social provider linked.") },
        endpoint(HttpMethod.Delete, "/auth/social/unlink/{provider}", "Auth", "Unlink social account", roles = setOf(PbRole.USER)) { call ->
            message("Social provider unlinked.", "provider" to call.pathParam("provider", "google"))
        },
    )

private fun userEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Get, "/users/me", "User", "Current user", roles = setOf(PbRole.USER)) {
            StubResponse(
                data =
                    mapOf(
                        "id" to 1,
                        "email" to "user@pbshop.dev",
                        "name" to "PB User",
                        "role" to "USER",
                        "badges" to listOf("REVIEW_MASTER"),
                    ),
            )
        },
        endpoint(HttpMethod.Get, "/users", "User", "Admin user list", roles = setOf(PbRole.ADMIN)) {
            paged(
                listOf(
                    mapOf("id" to 1, "email" to "user@pbshop.dev", "role" to "USER"),
                    mapOf("id" to 2, "email" to "seller@pbshop.dev", "role" to "SELLER"),
                ),
            )
        },
    )

private fun catalogEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Get, "/categories", "Category", "Category list") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Laptop", "slug" to "laptop"), mapOf("id" to 2, "name" to "PC Parts", "slug" to "pc-parts")))
        },
        endpoint(HttpMethod.Post, "/categories", "Category", "Create category", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "name" to "Tablet", "slug" to "tablet"))
        },
        endpoint(HttpMethod.Get, "/products", "Product", "Product list") {
            paged(listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")), totalCount = 2)
        },
        endpoint(HttpMethod.Get, "/products/{id}", "Product", "Product detail") { call ->
            StubResponse(
                data =
                    mapOf(
                        "id" to call.pathParam("id", "1"),
                        "name" to "PB GalaxyBook 4 Pro",
                        "description" to "Kotlin baseline product detail payload.",
                        "lowestPrice" to 1590000,
                        "thumbnailUrl" to "/images/products/1-thumb.webp",
                        "specs" to mapOf("cpu" to "Ryzen 7", "ram" to "32GB"),
                    ),
            )
        },
        endpoint(HttpMethod.Post, "/products", "Product", "Create product", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = productSummary(101, "PB New Product"))
        },
        endpoint(HttpMethod.Get, "/specs/definitions", "Spec", "Specification definitions") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "CPU", "categoryId" to 2), mapOf("id" to 2, "name" to "RAM", "categoryId" to 2)))
        },
        endpoint(HttpMethod.Post, "/specs/compare", "Spec", "Compare specifications") {
            StubResponse(data = mapOf("items" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")), "diff" to listOf("cpu", "weight", "battery")))
        },
        endpoint(HttpMethod.Post, "/specs/compare/scored", "Spec", "Compare specifications with score") {
            StubResponse(data = mapOf("winnerProductId" to 1, "scores" to listOf(mapOf("productId" to 1, "score" to 91), mapOf("productId" to 2, "score" to 88))))
        },
        endpoint(HttpMethod.Get, "/sellers", "Seller", "Seller list") {
            paged(listOf(mapOf("id" to 1, "name" to "PB Mall", "trustScore" to 95), mapOf("id" to 2, "name" to "Fast Delivery", "trustScore" to 92)))
        },
        endpoint(HttpMethod.Post, "/sellers", "Seller", "Create seller", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "name" to "New Seller"))
        },
        endpoint(HttpMethod.Get, "/products/{id}/prices", "Price", "Product prices") {
            StubResponse(data = listOf(mapOf("sellerId" to 1, "sellerName" to "PB Mall", "price" to 1590000), mapOf("sellerId" to 2, "sellerName" to "Fast Delivery", "price" to 1585000)))
        },
        endpoint(HttpMethod.Get, "/products/{id}/price-history", "Price", "Product price history") {
            StubResponse(data = listOf(mapOf("date" to "2026-03-20", "price" to 1600000), mapOf("date" to "2026-03-21", "price" to 1595000), mapOf("date" to "2026-03-22", "price" to 1590000)))
        },
        endpoint(HttpMethod.Post, "/price-alerts", "Price", "Create price alert", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "targetPrice" to 1490000, "enabled" to true))
        },
        endpoint(HttpMethod.Get, "/price-alerts", "Price", "Price alert list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "productId" to 1, "targetPrice" to 1490000)))
        },
    )

private fun commerceEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Get, "/cart", "Cart", "Get cart", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "productId" to 1, "quantity" to 2, "price" to 1590000)))
        },
        endpoint(HttpMethod.Post, "/cart", "Cart", "Add cart item", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "productId" to 2, "quantity" to 1))
        },
        endpoint(HttpMethod.Patch, "/cart/{itemId}", "Cart", "Update cart item", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("itemId", "1"), "quantity" to 3))
        },
        endpoint(HttpMethod.Delete, "/cart/{itemId}", "Cart", "Delete cart item", roles = setOf(PbRole.USER)) { message("Cart item removed.") },
        endpoint(HttpMethod.Delete, "/cart", "Cart", "Clear cart", roles = setOf(PbRole.USER)) { message("Cart cleared.") },
        endpoint(HttpMethod.Get, "/addresses", "Address", "Address list", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "recipient" to "PB User", "city" to "Seoul", "isDefault" to true)))
        },
        endpoint(HttpMethod.Post, "/addresses", "Address", "Create address", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "recipient" to "PB User", "city" to "Busan"))
        },
        endpoint(HttpMethod.Patch, "/addresses/{id}", "Address", "Update address", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "recipient" to "PB User", "city" to "Seoul"))
        },
        endpoint(HttpMethod.Delete, "/addresses/{id}", "Address", "Delete address", roles = setOf(PbRole.USER)) { message("Address deleted.") },
        endpoint(HttpMethod.Post, "/orders", "Order", "Create order", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "orderNumber" to "ORD-20260323-KOTLIN", "status" to "ORDER_PLACED", "finalAmount" to 3175000))
        },
        endpoint(HttpMethod.Get, "/orders", "Order", "Order list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "orderNumber" to "ORD-20260323-KOTLIN", "status" to "PAYMENT_CONFIRMED")))
        },
        endpoint(HttpMethod.Get, "/orders/{id}", "Order", "Order detail", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "PAYMENT_CONFIRMED", "items" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"))))
        },
        endpoint(HttpMethod.Post, "/orders/{id}/cancel", "Order", "Cancel order", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "CANCELLED"))
        },
        endpoint(HttpMethod.Get, "/admin/orders", "Order", "Admin order list", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("id" to 1, "status" to "PAYMENT_CONFIRMED"), mapOf("id" to 2, "status" to "SHIPPING")))
        },
        endpoint(HttpMethod.Patch, "/admin/orders/{id}/status", "Order", "Admin update order status", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "SHIPPING"))
        },
        endpoint(HttpMethod.Post, "/payments", "Payment", "Create payment", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "PAYMENT_CONFIRMED", "provider" to "PBPAY"))
        },
        endpoint(HttpMethod.Get, "/payments/{id}", "Payment", "Payment detail", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "PAYMENT_CONFIRMED"))
        },
        endpoint(HttpMethod.Post, "/payments/{id}/refund", "Payment", "Refund payment", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "REFUND_REQUESTED"))
        },
        endpoint(HttpMethod.Get, "/products/{productId}/reviews", "Review", "Product reviews") { paged(listOf(mapOf("id" to 1, "rating" to 5, "content" to "Great product."))) },
        endpoint(HttpMethod.Post, "/products/{productId}/reviews", "Review", "Create product review", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 10, "rating" to 5, "awardedPoint" to 500))
        },
        endpoint(HttpMethod.Patch, "/reviews/{id}", "Review", "Update review", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "content" to "Updated review"))
        },
        endpoint(HttpMethod.Delete, "/reviews/{id}", "Review", "Delete review", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Review deleted.") },
        endpoint(HttpMethod.Get, "/wishlist", "Wishlist", "Wishlist list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("productId" to 1, "wishlistedAt" to "2026-03-23T10:00:00Z")))
        },
        endpoint(HttpMethod.Post, "/wishlist/{productId}", "Wishlist", "Toggle wishlist", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("wishlisted" to true)) },
        endpoint(HttpMethod.Delete, "/wishlist/{productId}", "Wishlist", "Delete wishlist item", roles = setOf(PbRole.USER)) { message("Wishlist item removed.") },
        endpoint(HttpMethod.Get, "/points/balance", "Point", "Point balance", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("balance" to 15000, "expiringSoon" to 3000, "expiringDate" to "2026-03-01"))
        },
        endpoint(HttpMethod.Get, "/points/transactions", "Point", "Point transactions", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "type" to "EARN", "amount" to 500, "balance" to 15000)))
        },
        endpoint(HttpMethod.Post, "/admin/points/grant", "Point", "Grant points", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "type" to "ADMIN_GRANT", "amount" to 1000))
        },
    )

private fun engagementEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Get, "/boards", "Community", "Board list") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Reviews"), mapOf("id" to 2, "name" to "Deals")))
        },
        endpoint(HttpMethod.Get, "/boards/{boardId}/posts", "Community", "Board posts") { paged(listOf(mapOf("id" to 1, "title" to "GalaxyBook 4 usage review", "likeCount" to 45))) },
        endpoint(HttpMethod.Get, "/posts/{id}", "Community", "Post detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "GalaxyBook 4 usage review", "content" to "Detailed impressions"))
        },
        endpoint(HttpMethod.Post, "/boards/{boardId}/posts", "Community", "Create post", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 5, "title" to "New community post"))
        },
        endpoint(HttpMethod.Patch, "/posts/{id}", "Community", "Update post", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated post"))
        },
        endpoint(HttpMethod.Delete, "/posts/{id}", "Community", "Delete post", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Post deleted.") },
        endpoint(HttpMethod.Post, "/posts/{id}/like", "Community", "Toggle post like", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("liked" to true, "likeCount" to 46)) },
        endpoint(HttpMethod.Get, "/posts/{id}/comments", "Community", "Post comments") { StubResponse(data = listOf(mapOf("id" to 1, "content" to "Helpful review."))) },
        endpoint(HttpMethod.Post, "/posts/{id}/comments", "Community", "Create post comment", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "content" to "Great insight."))
        },
        endpoint(HttpMethod.Delete, "/comments/{id}", "Community", "Delete post comment", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Comment deleted.") },
        endpoint(HttpMethod.Get, "/products/{productId}/inquiries", "Inquiry", "Product inquiries") { paged(listOf(mapOf("id" to 1, "title" to "Battery spec", "isSecret" to false))) },
        endpoint(HttpMethod.Post, "/products/{productId}/inquiries", "Inquiry", "Create inquiry", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "status" to "OPEN"))
        },
        endpoint(HttpMethod.Post, "/inquiries/{id}/answer", "Inquiry", "Answer inquiry", roles = setOf(PbRole.SELLER, PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "ANSWERED"))
        },
        endpoint(HttpMethod.Get, "/inquiries/me", "Inquiry", "My inquiries", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "title" to "Battery spec", "status" to "OPEN"))) },
        endpoint(HttpMethod.Delete, "/inquiries/{id}", "Inquiry", "Delete inquiry", roles = setOf(PbRole.USER)) { message("Inquiry deleted.") },
        endpoint(HttpMethod.Get, "/support/tickets", "Support", "Support tickets", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "ticketNumber" to "TK-20260323-001", "status" to "OPEN"))) },
        endpoint(HttpMethod.Post, "/support/tickets", "Support", "Create support ticket", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "ticketNumber" to "TK-20260323-002", "status" to "OPEN"))
        },
        endpoint(HttpMethod.Get, "/support/tickets/{id}", "Support", "Support ticket detail", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "OPEN", "replies" to emptyList<String>()))
        },
        endpoint(HttpMethod.Post, "/support/tickets/{id}/reply", "Support", "Reply support ticket", roles = setOf(PbRole.USER, PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to call.pathParam("id", "1"), "content" to "Reply created"))
        },
        endpoint(HttpMethod.Get, "/admin/support/tickets", "Support", "Admin support ticket list", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("id" to 1, "status" to "OPEN"), mapOf("id" to 2, "status" to "RESOLVED")))
        },
        endpoint(HttpMethod.Patch, "/admin/support/tickets/{id}/status", "Support", "Admin update support ticket status", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "RESOLVED"))
        },
        endpoint(HttpMethod.Get, "/faqs", "Help", "FAQ list") { StubResponse(data = listOf(mapOf("id" to 1, "question" to "How do I cancel an order?"))) },
        endpoint(HttpMethod.Post, "/faqs", "Help", "Create FAQ", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "question" to "How do I reset my password?"))
        },
        endpoint(HttpMethod.Patch, "/faqs/{id}", "Help", "Update FAQ", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "question" to "Updated FAQ")) },
        endpoint(HttpMethod.Delete, "/faqs/{id}", "Help", "Delete FAQ", roles = setOf(PbRole.ADMIN)) { message("FAQ deleted.") },
        endpoint(HttpMethod.Get, "/notices", "Help", "Notice list") { paged(listOf(mapOf("id" to 1, "title" to "Service maintenance notice"))) },
        endpoint(HttpMethod.Get, "/notices/{id}", "Help", "Notice detail") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Service maintenance notice", "content" to "Maintenance schedule")) },
        endpoint(HttpMethod.Post, "/notices", "Help", "Create notice", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "title" to "New notice")) },
        endpoint(HttpMethod.Patch, "/notices/{id}", "Help", "Update notice", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated notice")) },
        endpoint(HttpMethod.Delete, "/notices/{id}", "Help", "Delete notice", roles = setOf(PbRole.ADMIN)) { message("Notice deleted.") },
        endpoint(HttpMethod.Get, "/activity/views", "Activity", "Recently viewed products", roles = setOf(PbRole.USER)) { paged(listOf(productSummary(1, "PB GalaxyBook 4 Pro"))) },
        endpoint(HttpMethod.Delete, "/activity/views", "Activity", "Clear view history", roles = setOf(PbRole.USER)) { message("View history cleared.") },
        endpoint(HttpMethod.Get, "/activity/searches", "Activity", "Search history", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "keyword" to "galaxybook"), mapOf("id" to 2, "keyword" to "7800x3d")))
        },
        endpoint(HttpMethod.Delete, "/activity/searches", "Activity", "Clear search history", roles = setOf(PbRole.USER)) { message("Search history cleared.") },
        endpoint(HttpMethod.Delete, "/activity/searches/{id}", "Activity", "Delete search history entry", roles = setOf(PbRole.USER)) { message("Search history entry deleted.") },
        endpoint(HttpMethod.Post, "/chat/rooms", "Chat", "Create chat room", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "OPEN")) },
        endpoint(HttpMethod.Get, "/chat/rooms", "Chat", "Chat room list", roles = setOf(PbRole.USER, PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "status" to "OPEN"), mapOf("id" to 2, "status" to "CLOSED"))) },
        endpoint(HttpMethod.Get, "/chat/rooms/{id}/messages", "Chat", "Chat room messages", roles = setOf(PbRole.USER, PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "content" to "How can I help?"))) },
        endpoint(HttpMethod.Patch, "/chat/rooms/{id}/close", "Chat", "Close chat room", roles = setOf(PbRole.USER, PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "CLOSED")) },
        endpoint(HttpMethod.Post, "/chat/rooms/{id}/join", "Chat", "Join chat room", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Joined chat room.") },
        endpoint(HttpMethod.Post, "/chat/rooms/{id}/messages", "Chat", "Send chat message", roles = setOf(PbRole.USER, PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "content" to "Message sent")) },
    )

private fun discoveryEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Get, "/rankings/products/popular", "Ranking", "Popular product ranking") {
            StubResponse(data = listOf(mapOf("rank" to 1, "product" to productSummary(1, "PB GalaxyBook 4 Pro"), "score" to 15230)))
        },
        endpoint(HttpMethod.Get, "/rankings/searches", "Ranking", "Popular search ranking") {
            StubResponse(data = listOf(mapOf("rank" to 1, "keyword" to "galaxybook", "searchCount" to 5230)))
        },
        endpoint(HttpMethod.Get, "/recommendations/today", "Recommendation", "Today recommendations") {
            StubResponse(data = listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")))
        },
        endpoint(HttpMethod.Get, "/recommendations/personalized", "Recommendation", "Personalized recommendations", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(productSummary(3, "PB Gaming Laptop 17")))
        },
        endpoint(HttpMethod.Get, "/admin/recommendations", "Recommendation", "Admin recommendation list", roles = setOf(PbRole.ADMIN)) { StubResponse(data = listOf(mapOf("id" to 1, "productId" to 1, "slot" to "today"))) },
        endpoint(HttpMethod.Post, "/admin/recommendations", "Recommendation", "Create recommendation", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "productId" to 2, "slot" to "today")) },
        endpoint(HttpMethod.Delete, "/admin/recommendations/{id}", "Recommendation", "Delete recommendation", roles = setOf(PbRole.ADMIN)) { message("Recommendation deleted.") },
        endpoint(HttpMethod.Get, "/deals", "Deal", "Deal list") { paged(listOf(mapOf("id" to 1, "title" to "Spring laptop sale", "discountRate" to 15))) },
        endpoint(HttpMethod.Get, "/deals/{id}", "Deal", "Deal detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Spring laptop sale", "products" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"))))
        },
        endpoint(HttpMethod.Post, "/deals", "Deal", "Create deal", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "title" to "New deal")) },
        endpoint(HttpMethod.Patch, "/deals/{id}", "Deal", "Update deal", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated deal")) },
        endpoint(HttpMethod.Delete, "/deals/{id}", "Deal", "Delete deal", roles = setOf(PbRole.ADMIN)) { message("Deal deleted.") },
        endpoint(HttpMethod.Get, "/search", "Search", "Search catalog") {
            StubResponse(data = mapOf("hits" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")), "facets" to mapOf("categories" to listOf(mapOf("id" to 1, "name" to "Laptop", "count" to 12))), "suggestions" to listOf("galaxybook", "galaxybook 4"), "totalCount" to 1), meta = mapOf("page" to 1, "limit" to 20, "totalCount" to 1, "totalPages" to 1))
        },
        endpoint(HttpMethod.Get, "/search/autocomplete", "Search", "Search autocomplete") {
            StubResponse(data = mapOf("keywords" to listOf("galaxybook 4 pro"), "products" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")), "categories" to listOf(mapOf("id" to 1, "name" to "Laptop"))))
        },
        endpoint(HttpMethod.Get, "/search/popular", "Search", "Popular search keywords") { StubResponse(data = listOf(mapOf("keyword" to "galaxybook", "count" to 5230))) },
        endpoint(HttpMethod.Post, "/search/recent", "Search", "Save recent search", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "keyword" to "galaxybook", "createdAt" to "2026-03-23T10:00:00Z")) },
        endpoint(HttpMethod.Get, "/search/recent", "Search", "Recent searches", roles = setOf(PbRole.USER)) { StubResponse(data = listOf(mapOf("id" to 1, "keyword" to "galaxybook"), mapOf("id" to 2, "keyword" to "7800x3d"))) },
        endpoint(HttpMethod.Delete, "/search/recent/{id}", "Search", "Delete recent search", roles = setOf(PbRole.USER)) { message("Recent search deleted.") },
        endpoint(HttpMethod.Delete, "/search/recent", "Search", "Delete all recent searches", roles = setOf(PbRole.USER)) { message("Recent search history cleared.") },
        endpoint(HttpMethod.Patch, "/search/preferences", "Search", "Update search preferences", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("saveRecentSearches" to true)) },
        endpoint(HttpMethod.Get, "/search/admin/weights", "Search", "Search weights", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("nameWeight" to 1.0, "keywordWeight" to 1.5, "clickWeight" to 0.8)) },
        endpoint(HttpMethod.Patch, "/search/admin/weights", "Search", "Update search weights", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("nameWeight" to 1.2, "keywordWeight" to 1.4, "clickWeight" to 0.9)) },
        endpoint(HttpMethod.Get, "/search/admin/index/status", "Search", "Search index status", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("status" to "HEALTHY", "documents" to 1200, "lastIndexedAt" to "2026-03-23T09:55:00Z")) },
        endpoint(HttpMethod.Post, "/search/admin/index/reindex", "Search", "Reindex search", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "Reindex queued.", "queued" to true)) },
        endpoint(HttpMethod.Post, "/search/admin/index/products/{id}/reindex", "Search", "Reindex product", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "Product reindex queued.", "productId" to call.pathParam("id", "1"))) },
        endpoint(HttpMethod.Get, "/search/admin/index/outbox/summary", "Search", "Search outbox summary", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("pending" to 2, "failed" to 1, "lastProcessedAt" to "2026-03-23T09:58:00Z")) },
        endpoint(HttpMethod.Post, "/search/admin/index/outbox/requeue-failed", "Search", "Requeue failed outbox entries", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("requeuedCount" to 1)) },
        endpoint(HttpMethod.Post, "/crawler/admin/jobs", "Crawler", "Create crawler job", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "name" to "Coupang laptop crawler", "status" to "IDLE")) },
        endpoint(HttpMethod.Get, "/crawler/admin/jobs", "Crawler", "Crawler job list", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "name" to "Coupang laptop crawler", "status" to "IDLE"))) },
        endpoint(HttpMethod.Patch, "/crawler/admin/jobs/{id}", "Crawler", "Update crawler job", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "UPDATED")) },
        endpoint(HttpMethod.Delete, "/crawler/admin/jobs/{id}", "Crawler", "Delete crawler job", roles = setOf(PbRole.ADMIN)) { message("Crawler job deleted.") },
        endpoint(HttpMethod.Post, "/crawler/admin/jobs/{id}/run", "Crawler", "Run crawler job", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("message" to "Crawler job queued.", "runId" to "run-${call.pathParam("id", "1")}")) },
        endpoint(HttpMethod.Post, "/crawler/admin/triggers", "Crawler", "Trigger crawler", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("message" to "Crawler trigger queued.", "runId" to "run-trigger-1")) },
        endpoint(HttpMethod.Get, "/crawler/admin/runs", "Crawler", "Crawler runs", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "status" to "SUCCESS", "itemsProcessed" to 245))) },
        endpoint(HttpMethod.Get, "/crawler/admin/monitoring", "Crawler", "Crawler monitoring summary", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("queueDepth" to 2, "failedRuns" to 1, "lastSuccessAt" to "2026-03-23T09:55:00Z")) },
        endpoint(HttpMethod.Get, "/predictions/products/{productId}/price-trend", "Prediction", "Price trend prediction") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("productId", "1"), "trend" to "FALLING", "recommendation" to "BUY_SOON", "predictions" to listOf(mapOf("date" to "2026-03-24", "predictedPrice" to 1585000, "confidence" to 0.85))))
        },
        endpoint(HttpMethod.Get, "/sellers/{id}/trust", "Trust", "Seller trust detail") { call -> StubResponse(data = mapOf("sellerId" to call.pathParam("id", "1"), "overallScore" to 95, "grade" to "A+")) },
        endpoint(HttpMethod.Get, "/sellers/{id}/reviews", "Trust", "Seller reviews") { paged(listOf(mapOf("id" to 1, "rating" to 5, "content" to "Fast delivery"))) },
        endpoint(HttpMethod.Post, "/sellers/{id}/reviews", "Trust", "Create seller review", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "rating" to 5)) },
        endpoint(HttpMethod.Patch, "/seller-reviews/{id}", "Trust", "Update seller review", roles = setOf(PbRole.USER)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "rating" to 4)) },
        endpoint(HttpMethod.Delete, "/seller-reviews/{id}", "Trust", "Delete seller review", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Seller review deleted.") },
        endpoint(HttpMethod.Get, "/i18n/translations", "I18n", "Translations") { StubResponse(data = listOf(mapOf("id" to 1, "key" to "product.lowest_price", "value" to "Lowest Price", "locale" to "en"))) },
        endpoint(HttpMethod.Post, "/admin/i18n/translations", "I18n", "Upsert translation", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "key" to "product.add_to_cart", "locale" to "en")) },
        endpoint(HttpMethod.Delete, "/admin/i18n/translations/{id}", "I18n", "Delete translation", roles = setOf(PbRole.ADMIN)) { message("Translation deleted.") },
        endpoint(HttpMethod.Get, "/i18n/exchange-rates", "I18n", "Exchange rates") { StubResponse(data = listOf(mapOf("baseCurrency" to "KRW", "targetCurrency" to "USD", "rate" to 0.000748))) },
        endpoint(HttpMethod.Get, "/i18n/convert", "I18n", "Currency conversion") { StubResponse(data = mapOf("originalAmount" to 1590000, "originalCurrency" to "KRW", "convertedAmount" to 1189.32, "targetCurrency" to "USD")) },
    )

private fun mediaEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Post, "/images/upload", "Image", "Upload image", roles = setOf(PbRole.USER, PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "originalUrl" to "/uploads/original/abc123.jpg", "variants" to listOf(mapOf("type" to "THUMBNAIL", "url" to "/uploads/thumb/abc123.webp")), "processingStatus" to "COMPLETED"))
        },
        endpoint(HttpMethod.Post, "/upload/image", "Image", "Legacy image upload", roles = setOf(PbRole.USER, PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("url" to "/uploads/original/legacy-image.jpg"))
        },
        endpoint(HttpMethod.Get, "/images/{id}/variants", "Image", "Image variants") { call ->
            StubResponse(data = listOf(mapOf("imageId" to call.pathParam("id", "1"), "type" to "THUMBNAIL", "url" to "/uploads/thumb/abc123.webp")))
        },
        endpoint(HttpMethod.Delete, "/images/{id}", "Image", "Delete image", roles = setOf(PbRole.ADMIN)) { message("Image deleted.") },
        endpoint(HttpMethod.Post, "/media/upload", "Media", "Upload media", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = listOf(mapOf("id" to 1, "mime" to "video/mp4", "ownerType" to "product")))
        },
        endpoint(HttpMethod.Post, "/media/presigned-url", "Media", "Create presigned URL", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("uploadUrl" to "https://storage.pbshop.dev/upload/1", "fileKey" to "media/1.mp4"))
        },
        endpoint(HttpMethod.Get, "/media/stream/{id}", "Media", "Stream media") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "streamUrl" to "/media/stream/${call.pathParam("id", "1")}"))
        },
        endpoint(HttpMethod.Delete, "/media/{id}", "Media", "Delete media", roles = setOf(PbRole.USER)) { message("Media deleted.") },
        endpoint(HttpMethod.Get, "/media/{id}/metadata", "Media", "Media metadata") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "mime" to "video/mp4", "size" to 1024000))
        },
        endpoint(HttpMethod.Get, "/badges", "Badge", "Badge list") { StubResponse(data = listOf(mapOf("id" to 1, "name" to "Review Master", "holderCount" to 1523))) },
        endpoint(HttpMethod.Get, "/badges/me", "Badge", "Current user badges", roles = setOf(PbRole.USER)) { StubResponse(data = listOf(mapOf("id" to 1, "name" to "Review Master"))) },
        endpoint(HttpMethod.Get, "/users/{id}/badges", "Badge", "User badges") { call -> StubResponse(data = listOf(mapOf("userId" to call.pathParam("id", "1"), "badge" to "Review Master"))) },
        endpoint(HttpMethod.Post, "/admin/badges", "Badge", "Create badge", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Purchase King")) },
        endpoint(HttpMethod.Patch, "/admin/badges/{id}", "Badge", "Update badge", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated badge")) },
        endpoint(HttpMethod.Delete, "/admin/badges/{id}", "Badge", "Delete badge", roles = setOf(PbRole.ADMIN)) { message("Badge deleted.") },
        endpoint(HttpMethod.Post, "/admin/badges/{id}/grant", "Badge", "Grant badge", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("badgeId" to call.pathParam("id", "1"), "userId" to 1))
        },
        endpoint(HttpMethod.Delete, "/admin/badges/{id}/revoke/{userId}", "Badge", "Revoke badge", roles = setOf(PbRole.ADMIN)) { message("Badge revoked.") },
        endpoint(HttpMethod.Post, "/push/subscriptions", "Push", "Create push subscription", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "endpoint" to "https://fcm.googleapis.com/fcm/send/..."))
        },
        endpoint(HttpMethod.Post, "/push/subscriptions/unsubscribe", "Push", "Unsubscribe push", roles = setOf(PbRole.USER)) { message("Push subscription disabled.", "success" to true) },
        endpoint(HttpMethod.Get, "/push/subscriptions", "Push", "Push subscription list", roles = setOf(PbRole.USER)) { StubResponse(data = listOf(mapOf("id" to 1, "isActive" to true))) },
        endpoint(HttpMethod.Get, "/push/preferences", "Push", "Push preferences", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("marketing" to true, "priceAlerts" to true)) },
        endpoint(HttpMethod.Post, "/push/preferences", "Push", "Update push preferences", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("marketing" to false, "priceAlerts" to true)) },
        endpoint(HttpMethod.Post, "/admin/push/send", "Push", "Send admin push", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("sentCount" to 1523, "scheduledAt" to null)) },
    )

private fun builderEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Get, "/pc-builds", "PC Builder", "PC build list", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "name" to "Gaming PC 2026", "purpose" to "GAMING"))) },
        endpoint(HttpMethod.Post, "/pc-builds", "PC Builder", "Create PC build", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "name" to "Gaming PC 2026", "totalPrice" to 0)) },
        endpoint(HttpMethod.Get, "/pc-builds/{id}", "PC Builder", "PC build detail") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "parts" to emptyList<String>(), "totalPrice" to 350000)) },
        endpoint(HttpMethod.Patch, "/pc-builds/{id}", "PC Builder", "Update PC build", roles = setOf(PbRole.USER)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated build")) },
        endpoint(HttpMethod.Delete, "/pc-builds/{id}", "PC Builder", "Delete PC build", roles = setOf(PbRole.USER)) { message("PC build deleted.") },
        endpoint(HttpMethod.Post, "/pc-builds/{id}/parts", "PC Builder", "Add PC part", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "parts" to listOf(mapOf("partType" to "CPU", "product" to productSummary(101, "AMD Ryzen 7 7800X3D")))))
        },
        endpoint(HttpMethod.Delete, "/pc-builds/{id}/parts/{partId}", "PC Builder", "Delete PC part", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "removedPartId" to call.pathParam("partId", "1")))
        },
        endpoint(HttpMethod.Get, "/pc-builds/{id}/compatibility", "PC Builder", "PC compatibility") { call ->
            StubResponse(data = mapOf("buildId" to call.pathParam("id", "1"), "status" to "WARNING", "warnings" to listOf("GPU is underpowered for the selected CPU.")))
        },
        endpoint(HttpMethod.Get, "/pc-builds/{id}/share", "PC Builder", "Create PC share link", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("shareUrl" to "https://pbshop.dev/pc-builds/shared/${call.pathParam("id", "1")}"))
        },
        endpoint(HttpMethod.Get, "/pc-builds/shared/{shareCode}", "PC Builder", "Shared PC build") { call ->
            StubResponse(data = mapOf("shareCode" to call.pathParam("shareCode", "PB-1234"), "name" to "Gaming PC 2026"))
        },
        endpoint(HttpMethod.Get, "/pc-builds/popular", "PC Builder", "Popular PC builds") { paged(listOf(mapOf("id" to 1, "name" to "Gaming PC 2026", "likes" to 120))) },
        endpoint(HttpMethod.Get, "/admin/compatibility-rules", "PC Builder", "Compatibility rule list", roles = setOf(PbRole.ADMIN)) { StubResponse(data = listOf(mapOf("id" to 1, "name" to "Socket match", "enabled" to true))) },
        endpoint(HttpMethod.Post, "/admin/compatibility-rules", "PC Builder", "Create compatibility rule", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Power headroom")) },
        endpoint(HttpMethod.Patch, "/admin/compatibility-rules/{id}", "PC Builder", "Update compatibility rule", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated compatibility rule"))
        },
        endpoint(HttpMethod.Delete, "/admin/compatibility-rules/{id}", "PC Builder", "Delete compatibility rule", roles = setOf(PbRole.ADMIN)) { message("Compatibility rule deleted.") },
        endpoint(HttpMethod.Post, "/friends/request/{userId}", "Friend", "Send friend request", roles = setOf(PbRole.USER)) { call -> message("Friend request sent.", "userId" to call.pathParam("userId", "1")) },
        endpoint(HttpMethod.Patch, "/friends/request/{friendshipId}/accept", "Friend", "Accept friend request", roles = setOf(PbRole.USER)) { message("Friend request accepted.") },
        endpoint(HttpMethod.Patch, "/friends/request/{friendshipId}/reject", "Friend", "Reject friend request", roles = setOf(PbRole.USER)) { message("Friend request rejected.") },
        endpoint(HttpMethod.Get, "/friends", "Friend", "Friend list", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 10, "name" to "PB Friend"))) },
        endpoint(HttpMethod.Get, "/friends/requests/received", "Friend", "Received friend requests", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "requesterId" to 10))) },
        endpoint(HttpMethod.Get, "/friends/requests/sent", "Friend", "Sent friend requests", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 2, "targetUserId" to 11))) },
        endpoint(HttpMethod.Get, "/friends/feed", "Friend", "Friend activity feed", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("type" to "REVIEW_CREATED", "actorId" to 10))) },
        endpoint(HttpMethod.Post, "/friends/block/{userId}", "Friend", "Block user", roles = setOf(PbRole.USER)) { message("User blocked.") },
        endpoint(HttpMethod.Delete, "/friends/block/{userId}", "Friend", "Unblock user", roles = setOf(PbRole.USER)) { message("User unblocked.") },
        endpoint(HttpMethod.Delete, "/friends/{userId}", "Friend", "Delete friend", roles = setOf(PbRole.USER)) { message("Friend removed.") },
        endpoint(HttpMethod.Post, "/shortforms", "Shortform", "Upload shortform", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "PROCESSING", "title" to "PB shortform")) },
        endpoint(HttpMethod.Get, "/shortforms", "Shortform", "Shortform feed") { paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "likeCount" to 120))) },
        endpoint(HttpMethod.Get, "/shortforms/{id}", "Shortform", "Shortform detail") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "PB shortform", "viewCount" to 1520)) },
        endpoint(HttpMethod.Post, "/shortforms/{id}/like", "Shortform", "Toggle shortform like", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("liked" to true, "likeCount" to 121)) },
        endpoint(HttpMethod.Post, "/shortforms/{id}/comments", "Shortform", "Create shortform comment", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "content" to "Nice clip")) },
        endpoint(HttpMethod.Get, "/shortforms/{id}/comments", "Shortform", "Shortform comments") { paged(listOf(mapOf("id" to 1, "content" to "Awesome video"))) },
        endpoint(HttpMethod.Get, "/shortforms/ranking/list", "Shortform", "Shortform ranking") { paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "rank" to 1))) },
        endpoint(HttpMethod.Get, "/shortforms/{id}/transcode-status", "Shortform", "Transcode status") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "COMPLETED")) },
        endpoint(HttpMethod.Post, "/shortforms/{id}/transcode/retry", "Shortform", "Retry transcode", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("message" to "Retry queued.", "queued" to true)) },
        endpoint(HttpMethod.Delete, "/shortforms/{id}", "Shortform", "Delete shortform", roles = setOf(PbRole.USER)) { message("Shortform deleted.") },
        endpoint(HttpMethod.Get, "/shortforms/user/{userId}", "Shortform", "User shortforms") { paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "userId" to 1))) },
        endpoint(HttpMethod.Get, "/news", "News", "News list") { paged(listOf(mapOf("id" to 1, "title" to "PB weekly hardware briefing"))) },
        endpoint(HttpMethod.Get, "/news/categories", "News", "News category list") { StubResponse(data = listOf(mapOf("id" to 1, "name" to "Hardware"))) },
        endpoint(HttpMethod.Get, "/news/{id}", "News", "News detail") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "PB weekly hardware briefing", "relatedProducts" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")))) },
        endpoint(HttpMethod.Post, "/news", "News", "Create news", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "title" to "New article")) },
        endpoint(HttpMethod.Patch, "/news/{id}", "News", "Update news", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated article")) },
        endpoint(HttpMethod.Delete, "/news/{id}", "News", "Delete news", roles = setOf(PbRole.ADMIN)) { message("News deleted.") },
        endpoint(HttpMethod.Post, "/news/categories", "News", "Create news category", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Deals")) },
        endpoint(HttpMethod.Delete, "/news/categories/{id}", "News", "Delete news category", roles = setOf(PbRole.ADMIN)) { message("News category deleted.") },
        endpoint(HttpMethod.Get, "/matching/pending", "Matching", "Pending product mappings", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "sourceName" to "Vendor product", "status" to "PENDING"))) },
        endpoint(HttpMethod.Patch, "/matching/{id}/approve", "Matching", "Approve product mapping", roles = setOf(PbRole.ADMIN)) { message("Product mapping approved.") },
        endpoint(HttpMethod.Patch, "/matching/{id}/reject", "Matching", "Reject product mapping", roles = setOf(PbRole.ADMIN)) { message("Product mapping rejected.") },
        endpoint(HttpMethod.Post, "/matching/auto-match", "Matching", "Run auto match", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("matchedCount" to 12, "pendingCount" to 3)) },
        endpoint(HttpMethod.Get, "/matching/stats", "Matching", "Product mapping stats", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("approved" to 120, "pending" to 8, "rejected" to 5)) },
    )

private fun opsEndpoints(): List<EndpointDefinition> =
    listOf(
        endpoint(HttpMethod.Get, "/fraud/alerts", "Fraud", "Fraud alerts", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "status" to "OPEN", "productId" to 1))) },
        endpoint(HttpMethod.Patch, "/fraud/alerts/{id}/approve", "Fraud", "Approve fraud alert", roles = setOf(PbRole.ADMIN)) { message("Fraud alert approved.") },
        endpoint(HttpMethod.Patch, "/fraud/alerts/{id}/reject", "Fraud", "Reject fraud alert", roles = setOf(PbRole.ADMIN)) { message("Fraud alert rejected.") },
        endpoint(HttpMethod.Get, "/products/{id}/real-price", "Fraud", "Real price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "productPrice" to 1500000, "shippingFee" to 3000, "totalPrice" to 1503000))
        },
        endpoint(HttpMethod.Get, "/analytics/products/{id}/lowest-ever", "Analytics", "Lowest ever price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "isLowestEver" to true, "lowestPrice" to 1490000))
        },
        endpoint(HttpMethod.Get, "/analytics/products/{id}/unit-price", "Analytics", "Unit price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "unitPrice" to 12450, "unit" to "GB"))
        },
        endpoint(HttpMethod.Get, "/used-market/products/{id}/price", "Used Market", "Used market product price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "averagePrice" to 980000, "trend" to "STABLE"))
        },
        endpoint(HttpMethod.Get, "/used-market/categories/{id}/prices", "Used Market", "Used market category prices") { paged(listOf(mapOf("categoryId" to 1, "averagePrice" to 540000))) },
        endpoint(HttpMethod.Post, "/used-market/pc-builds/{buildId}/estimate", "Used Market", "Used PC build estimate", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("buildId" to call.pathParam("buildId", "1"), "estimatedPrice" to 1200000))
        },
        endpoint(HttpMethod.Get, "/auto/models", "Auto", "Auto models") { StubResponse(data = listOf(mapOf("id" to 1, "brand" to "PB Motors", "name" to "Electro X"))) },
        endpoint(HttpMethod.Get, "/auto/models/{id}/trims", "Auto", "Auto trims") { call -> StubResponse(data = listOf(mapOf("modelId" to call.pathParam("id", "1"), "name" to "Long Range"))) },
        endpoint(HttpMethod.Post, "/auto/estimate", "Auto", "Auto estimate") { StubResponse(data = mapOf("basePrice" to 48000000, "optionPrice" to 3500000, "totalPrice" to 53200000)) },
        endpoint(HttpMethod.Get, "/auto/models/{id}/lease-offers", "Auto", "Auto lease offers") { call ->
            StubResponse(data = listOf(mapOf("modelId" to call.pathParam("id", "1"), "provider" to "PB Lease", "monthlyPayment" to 450000)))
        },
        endpoint(HttpMethod.Post, "/auctions", "Auction", "Create reverse auction", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "OPEN")) },
        endpoint(HttpMethod.Get, "/auctions", "Auction", "Auction list") { paged(listOf(mapOf("id" to 1, "status" to "OPEN", "title" to "Laptop bulk purchase"))) },
        endpoint(HttpMethod.Get, "/auctions/{id}", "Auction", "Auction detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "OPEN", "bids" to listOf(mapOf("id" to 1, "price" to 1490000))))
        },
        endpoint(HttpMethod.Post, "/auctions/{id}/bids", "Auction", "Create bid", roles = setOf(PbRole.SELLER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "price" to 1490000)) },
        endpoint(HttpMethod.Patch, "/auctions/{id}/bids/{bidId}/select", "Auction", "Select bid", roles = setOf(PbRole.USER)) { message("Bid selected.") },
        endpoint(HttpMethod.Delete, "/auctions/{id}", "Auction", "Delete auction", roles = setOf(PbRole.USER)) { message("Auction deleted.") },
        endpoint(HttpMethod.Patch, "/auctions/{id}/bids/{bidId}", "Auction", "Update bid", roles = setOf(PbRole.SELLER)) { call ->
            StubResponse(data = mapOf("bidId" to call.pathParam("bidId", "1"), "price" to 1480000))
        },
        endpoint(HttpMethod.Delete, "/auctions/{id}/bids/{bidId}", "Auction", "Delete bid", roles = setOf(PbRole.SELLER)) { message("Bid deleted.") },
        endpoint(HttpMethod.Post, "/compare/add", "Compare", "Add compare item") { StubResponse(data = mapOf("compareList" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")))) },
        endpoint(HttpMethod.Delete, "/compare/{productId}", "Compare", "Delete compare item") { StubResponse(data = mapOf("compareList" to emptyList<String>())) },
        endpoint(HttpMethod.Get, "/compare", "Compare", "Compare list") { StubResponse(data = mapOf("compareList" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")))) },
        endpoint(HttpMethod.Get, "/compare/detail", "Compare", "Compare detail") {
            StubResponse(data = mapOf("items" to listOf(productSummary(1, "PB GalaxyBook 4 Pro"), productSummary(2, "PB Creator Laptop 16")), "highlights" to listOf("weight", "battery")))
        },
        endpoint(HttpMethod.Get, "/admin/settings/extensions", "Admin Settings", "Upload extension settings", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("extensions" to listOf("jpg", "png", "webp", "mp4"))) },
        endpoint(HttpMethod.Post, "/admin/settings/extensions", "Admin Settings", "Update upload extension settings", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("extensions" to listOf("jpg", "png", "webp", "mp4", "mp3"))) },
        endpoint(HttpMethod.Get, "/admin/settings/upload-limits", "Admin Settings", "Upload limits", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("image" to 5, "video" to 100, "audio" to 20)) },
        endpoint(HttpMethod.Patch, "/admin/settings/upload-limits", "Admin Settings", "Update upload limits", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("image" to 8, "video" to 120, "audio" to 20)) },
        endpoint(HttpMethod.Get, "/admin/settings/review-policy", "Admin Settings", "Review policy", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("maxImageCount" to 10, "pointAmount" to 500)) },
        endpoint(HttpMethod.Patch, "/admin/settings/review-policy", "Admin Settings", "Update review policy", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("maxImageCount" to 12, "pointAmount" to 700)) },
        endpoint(HttpMethod.Get, "/resilience/circuit-breakers", "Resilience", "Circuit breakers", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("items" to listOf(mapOf("name" to "payment-provider", "state" to "CLOSED"))))
        },
        endpoint(HttpMethod.Get, "/resilience/circuit-breakers/policies", "Resilience", "Circuit breaker policies", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("items" to listOf(mapOf("name" to "payment-provider", "options" to mapOf("failureThreshold" to 5), "stats" to mapOf("successCount" to 120)))))
        },
        endpoint(HttpMethod.Get, "/resilience/circuit-breakers/{name}", "Resilience", "Circuit breaker detail", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("name" to call.pathParam("name", "payment-provider"), "state" to "CLOSED"))
        },
        endpoint(HttpMethod.Post, "/resilience/circuit-breakers/{name}/reset", "Resilience", "Reset circuit breaker", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("message" to "Circuit breaker reset.", "name" to call.pathParam("name", "payment-provider")))
        },
        endpoint(HttpMethod.Get, "/errors/codes", "Error Code", "Error code catalog") { StubResponse(data = mapOf("total" to 4, "items" to listOf(mapOf("code" to "PRODUCT_NOT_FOUND", "message" to "Product not found")))) },
        endpoint(HttpMethod.Get, "/errors/codes/{key}", "Error Code", "Error code detail") { call ->
            StubResponse(data = mapOf("code" to call.pathParam("key", "PRODUCT_NOT_FOUND"), "message" to "Product not found"))
        },
        endpoint(HttpMethod.Get, "/admin/queues/supported", "Queue Admin", "Supported queues", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("items" to listOf("crawler", "image-processing", "search-index"))) },
        endpoint(HttpMethod.Get, "/admin/queues/stats", "Queue Admin", "Queue stats", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("total" to 3, "items" to listOf(mapOf("queueName" to "crawler", "paused" to false, "counts" to mapOf("failed" to 1, "waiting" to 2)))))
        },
        endpoint(HttpMethod.Post, "/admin/queues/auto-retry", "Queue Admin", "Auto retry failed jobs", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("retriedTotal" to 1, "items" to listOf(mapOf("queueName" to "crawler", "requeuedCount" to 1))))
        },
        endpoint(HttpMethod.Get, "/admin/queues/{queueName}/failed", "Queue Admin", "Failed queue jobs", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("jobId" to "job-1", "status" to "failed", "attempts" to 3))) },
        endpoint(HttpMethod.Post, "/admin/queues/{queueName}/failed/retry", "Queue Admin", "Retry failed queue jobs", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("requested" to 1, "requeuedCount" to 1, "jobIds" to listOf("job-1")))
        },
        endpoint(HttpMethod.Post, "/admin/queues/{queueName}/jobs/{jobId}/retry", "Queue Admin", "Retry single queue job", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("retried" to true)) },
        endpoint(HttpMethod.Delete, "/admin/queues/{queueName}/jobs/{jobId}", "Queue Admin", "Delete queue job", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("removed" to true)) },
        endpoint(HttpMethod.Get, "/admin/ops-dashboard/summary", "Ops Dashboard", "Ops dashboard summary", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("checkedAt" to "2026-03-23T10:00:00Z", "overallStatus" to "healthy", "health" to mapOf("db" to "UP", "redis" to "NOT_CONFIGURED", "elasticsearch" to "NOT_CONFIGURED"), "queue" to mapOf("failed" to 1), "alerts" to listOf<String>(), "alertCount" to 0))
        },
        endpoint(HttpMethod.Get, "/admin/observability/metrics", "Observability", "Observability metrics", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("totalRequests" to 1520, "errorRate" to 0.01, "avgLatencyMs" to 42)) },
        endpoint(HttpMethod.Get, "/admin/observability/traces", "Observability", "Observability traces", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("items" to listOf(mapOf("requestId" to "req-1", "method" to "GET", "path" to "/api/v1/products", "statusCode" to 200, "durationMs" to 24))))
        },
        endpoint(HttpMethod.Get, "/admin/observability/dashboard", "Observability", "Observability dashboard", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("process" to mapOf("uptimeSeconds" to 3600), "metrics" to mapOf("totalRequests" to 1520), "opsSummary" to mapOf("overallStatus" to "healthy")))
        },
        endpoint(HttpMethod.Get, "/query/products", "Query", "Query product read model") { paged(listOf(productSummary(1, "PB GalaxyBook 4 Pro"))) },
        endpoint(HttpMethod.Get, "/query/products/{productId}", "Query", "Query product read model detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("productId", "1"), "name" to "PB GalaxyBook 4 Pro", "source" to "query-model"))
        },
        endpoint(HttpMethod.Post, "/admin/query/products/{productId}/sync", "Query", "Sync single query product", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("productId" to call.pathParam("productId", "1"), "queued" to true))
        },
        endpoint(HttpMethod.Post, "/admin/query/products/rebuild", "Query", "Rebuild query read model", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("queued" to true, "scope" to "all-products")) },
        endpoint(HttpMethod.Post, "/products/{id}/images", "Image", "Attach product image", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("productId" to call.pathParam("id", "1"), "imageId" to 1))
        },
        endpoint(HttpMethod.Delete, "/products/{id}/images/{imageId}", "Image", "Delete product image", roles = setOf(PbRole.ADMIN)) { message("Product image deleted.") },
        endpoint(HttpMethod.Post, "/users/me/profile-image", "Image", "Upload profile image", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("profileImageUrl" to "/uploads/profile/user-1.webp")) },
        endpoint(HttpMethod.Delete, "/users/me/profile-image", "Image", "Delete profile image", roles = setOf(PbRole.USER)) { message("Profile image deleted.") },
    )

private fun endpoint(
    method: HttpMethod,
    path: String,
    tag: String,
    summary: String,
    roles: Set<PbRole> = emptySet(),
    handler: (ApplicationCall) -> StubResponse,
): EndpointDefinition =
    EndpointDefinition(
        method = method,
        path = path,
        tag = tag,
        summary = summary,
        roles = roles,
        handler = handler,
    )

private fun paged(
    items: List<Any?>,
    totalCount: Int = items.size,
    page: Int = 1,
    limit: Int = 20,
): StubResponse =
    StubResponse(
        data = items,
        meta =
            mapOf(
                "page" to page,
                "limit" to limit,
                "totalCount" to totalCount,
                "totalPages" to if (limit <= 0) 1 else ((totalCount + limit - 1) / limit),
            ),
    )

private fun message(
    text: String,
    vararg extras: Pair<String, Any?>,
): StubResponse =
    StubResponse(
        data =
            linkedMapOf<String, Any?>(
                "message" to text,
            ).apply {
                extras.forEach { (key, value) -> put(key, value) }
            },
    )

private fun productSummary(
    id: Int,
    name: String,
): Map<String, Any?> =
    mapOf(
        "id" to id,
        "name" to name,
        "lowestPrice" to 1590000,
        "thumbnailUrl" to "/images/products/$id-thumb.webp",
        "categoryName" to "Laptop",
    )

private fun ApplicationCall.pathParam(
    name: String,
    fallback: String,
): String = parameters[name] ?: fallback
