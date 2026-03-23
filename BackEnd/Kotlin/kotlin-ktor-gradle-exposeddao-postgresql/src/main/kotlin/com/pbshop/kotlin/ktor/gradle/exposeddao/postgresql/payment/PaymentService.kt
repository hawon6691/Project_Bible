package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class PaymentService(repository: PaymentRepository) : StubDomainService(repository)
