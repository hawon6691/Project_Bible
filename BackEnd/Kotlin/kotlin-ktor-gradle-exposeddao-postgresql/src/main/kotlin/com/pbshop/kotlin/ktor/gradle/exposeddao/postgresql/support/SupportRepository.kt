package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class SupportRepository : StubDomainRepository(supportOperations())
