package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class BadgeRepository : StubDomainRepository(badgeOperations())
