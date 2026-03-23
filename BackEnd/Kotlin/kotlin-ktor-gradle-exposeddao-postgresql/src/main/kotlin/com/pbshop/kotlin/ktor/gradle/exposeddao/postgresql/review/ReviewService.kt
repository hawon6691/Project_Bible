package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class ReviewService(repository: ReviewRepository) : StubDomainService(repository)
