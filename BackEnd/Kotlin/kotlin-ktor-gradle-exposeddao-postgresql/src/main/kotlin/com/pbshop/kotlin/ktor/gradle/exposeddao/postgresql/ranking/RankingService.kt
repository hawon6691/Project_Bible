package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class RankingService(repository: RankingRepository) : StubDomainService(repository)
