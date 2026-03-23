package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class ReviewRepository : StubDomainRepository(reviewOperations())
