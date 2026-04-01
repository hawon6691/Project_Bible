package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class QueueAdminController(
    private val service: QueueAdminService,
) {
    fun Route.register() {
        get("/admin/queues/supported") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.supportedQueues())
        }
        get("/admin/queues/stats") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.stats())
        }
        post("/admin/queues/auto-retry") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.autoRetry(
                    perQueueLimit = call.request.queryParameters["perQueueLimit"]?.toIntOrNull(),
                    maxTotal = call.request.queryParameters["maxTotal"]?.toIntOrNull(),
                ),
            )
        }
        get("/admin/queues/{queueName}/failed") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.failedJobs(
                    queueName = call.parameters["queueName"].orEmpty(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    newestFirst = call.request.queryParameters["newestFirst"] != "false",
                ),
            )
        }
        post("/admin/queues/{queueName}/failed/retry") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.retryFailedJobs(
                    queueName = call.parameters["queueName"].orEmpty(),
                    limit = call.request.queryParameters["limit"]?.toIntOrNull(),
                ),
            )
        }
        post("/admin/queues/{queueName}/jobs/{jobId}/retry") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.retryJob(call.parameters["queueName"].orEmpty(), call.parameters["jobId"].orEmpty()))
        }
        delete("/admin/queues/{queueName}/jobs/{jobId}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.removeJob(call.parameters["queueName"].orEmpty(), call.parameters["jobId"].orEmpty()))
        }
    }
}
