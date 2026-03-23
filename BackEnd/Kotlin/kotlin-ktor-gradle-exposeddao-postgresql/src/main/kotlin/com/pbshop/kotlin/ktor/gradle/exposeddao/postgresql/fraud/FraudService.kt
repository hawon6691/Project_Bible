package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class FraudService(repository: FraudRepository) : StubDomainService(repository)
