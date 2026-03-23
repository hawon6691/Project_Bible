package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class ProductRepository : StubDomainRepository(productOperations())
