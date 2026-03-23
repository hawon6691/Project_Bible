package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class CommunityRepository : StubDomainRepository(communityOperations())
