package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class ShortformRepository : StubDomainRepository(shortformOperations())
