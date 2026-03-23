package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class FraudRepository : StubDomainRepository(fraudOperations())
