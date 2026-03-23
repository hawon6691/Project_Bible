package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class AdminSettingsRepository : StubDomainRepository(adminSettingsOperations())
