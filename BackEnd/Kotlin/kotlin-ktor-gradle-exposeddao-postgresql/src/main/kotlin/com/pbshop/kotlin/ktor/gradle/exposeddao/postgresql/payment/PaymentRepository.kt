package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.payment

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class PaymentRepository : StubDomainRepository(paymentOperations())
