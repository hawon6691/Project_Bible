package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class AutoRepository : StubDomainRepository(autoOperations())
