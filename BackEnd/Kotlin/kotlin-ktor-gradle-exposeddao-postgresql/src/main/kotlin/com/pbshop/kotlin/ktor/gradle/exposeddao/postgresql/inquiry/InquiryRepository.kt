package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class InquiryRepository : StubDomainRepository(inquiryOperations())
