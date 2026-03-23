package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.resilience

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class ResilienceService(repository: ResilienceRepository) : StubDomainService(repository)
