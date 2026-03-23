package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class RecommendationService(repository: RecommendationRepository) : StubDomainService(repository)
