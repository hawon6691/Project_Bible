package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository

class PredictionRepository : StubDomainRepository(predictionOperations())
