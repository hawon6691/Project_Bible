package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class AddressRepository : StubDomainRepository(addressOperations())
