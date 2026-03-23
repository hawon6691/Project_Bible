package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class NewsService(repository: NewsRepository) : StubDomainService(repository)
