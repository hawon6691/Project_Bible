package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class UserService(repository: UserRepository) : StubDomainService(repository)
