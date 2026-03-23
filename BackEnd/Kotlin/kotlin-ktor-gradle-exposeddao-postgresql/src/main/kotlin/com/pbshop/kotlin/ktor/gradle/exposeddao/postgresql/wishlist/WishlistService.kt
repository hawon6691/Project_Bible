package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class WishlistService(repository: WishlistRepository) : StubDomainService(repository)
