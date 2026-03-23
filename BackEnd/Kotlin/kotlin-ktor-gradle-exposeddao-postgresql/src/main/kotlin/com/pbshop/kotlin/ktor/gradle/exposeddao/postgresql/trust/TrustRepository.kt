package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.trust

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class TrustRepository : StubDomainRepository(trustOperations())
