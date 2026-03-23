package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class CrawlerRepository : StubDomainRepository(crawlerOperations())
