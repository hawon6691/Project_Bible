package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class RankingRepository : StubDomainRepository(rankingOperations())
