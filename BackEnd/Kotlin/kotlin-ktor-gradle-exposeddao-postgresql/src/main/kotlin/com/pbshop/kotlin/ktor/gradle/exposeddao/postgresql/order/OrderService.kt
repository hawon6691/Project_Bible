package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.order

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class OrderService(repository: OrderRepository) : StubDomainService(repository)
