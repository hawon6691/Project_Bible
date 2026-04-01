package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.opsdashboard

interface OpsDashboardRepository {
    fun buildSummary(
        health: Map<String, Any?>?,
        searchSync: Map<String, Any?>?,
        crawler: Map<String, Any?>?,
        queue: Map<String, Any?>?,
        errors: Map<String, String>,
    ): Map<String, Any?>
}
