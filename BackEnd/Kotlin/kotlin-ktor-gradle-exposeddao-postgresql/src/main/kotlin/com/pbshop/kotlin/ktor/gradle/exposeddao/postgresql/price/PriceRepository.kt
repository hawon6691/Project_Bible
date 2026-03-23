package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class PriceRepository : StubDomainRepository(priceOperations())
