package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class OpsDashboardRepository : StubDomainRepository(opsDashboardOperations())
