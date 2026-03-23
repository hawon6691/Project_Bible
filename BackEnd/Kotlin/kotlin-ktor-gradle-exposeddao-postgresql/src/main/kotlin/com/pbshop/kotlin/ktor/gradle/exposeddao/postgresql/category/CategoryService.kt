package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService

class CategoryService(repository: CategoryRepository) : StubDomainService(repository)
