package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class FriendService(repository: FriendRepository) : StubDomainService(repository)
