package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class CompareRepository : StubDomainRepository(compareOperations())
