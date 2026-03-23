package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class PriceService(repository: PriceRepository) : StubDomainService(repository)
