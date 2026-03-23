package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class AnalyticsRepository : StubDomainRepository(analyticsOperations())
