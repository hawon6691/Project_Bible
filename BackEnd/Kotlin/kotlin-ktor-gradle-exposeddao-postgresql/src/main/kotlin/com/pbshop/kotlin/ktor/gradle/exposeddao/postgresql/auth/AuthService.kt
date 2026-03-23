package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class AuthService(repository: AuthRepository) : StubDomainService(repository)
