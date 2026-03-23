package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class ChatRepository : StubDomainRepository(chatOperations())
