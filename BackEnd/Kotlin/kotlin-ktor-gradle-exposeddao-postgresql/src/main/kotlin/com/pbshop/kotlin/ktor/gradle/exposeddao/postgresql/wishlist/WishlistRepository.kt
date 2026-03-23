package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class WishlistRepository : StubDomainRepository(wishlistOperations())
