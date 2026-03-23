package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class PushService(repository: PushRepository) : StubDomainService(repository)
