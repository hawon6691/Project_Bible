package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class ProductService(repository: ProductRepository) : StubDomainService(repository)
