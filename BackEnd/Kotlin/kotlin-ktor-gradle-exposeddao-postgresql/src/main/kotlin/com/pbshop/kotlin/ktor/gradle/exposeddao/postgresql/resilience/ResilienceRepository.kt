package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class ResilienceRepository : StubDomainRepository(resilienceOperations())
