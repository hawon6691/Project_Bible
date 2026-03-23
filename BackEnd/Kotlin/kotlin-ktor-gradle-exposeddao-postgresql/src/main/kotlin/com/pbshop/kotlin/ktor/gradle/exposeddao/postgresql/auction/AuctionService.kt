package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class AuctionService(repository: AuctionRepository) : StubDomainService(repository)
