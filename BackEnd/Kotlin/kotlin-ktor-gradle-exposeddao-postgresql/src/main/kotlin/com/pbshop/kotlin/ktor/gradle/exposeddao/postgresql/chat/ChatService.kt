package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class ChatService(repository: ChatRepository) : StubDomainService(repository)
