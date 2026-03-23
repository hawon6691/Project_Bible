package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class AuctionRepository : StubDomainRepository(auctionOperations())
