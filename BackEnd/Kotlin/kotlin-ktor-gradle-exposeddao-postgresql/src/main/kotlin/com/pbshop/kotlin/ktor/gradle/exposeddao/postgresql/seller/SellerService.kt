package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class SellerService(repository: SellerRepository) : StubDomainService(repository)
