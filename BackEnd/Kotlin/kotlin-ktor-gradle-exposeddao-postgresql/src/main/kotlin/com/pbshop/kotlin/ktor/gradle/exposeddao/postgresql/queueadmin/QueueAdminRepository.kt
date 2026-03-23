package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class QueueAdminRepository : StubDomainRepository(queueAdminOperations())
