package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class CartService(repository: CartRepository) : StubDomainService(repository)
