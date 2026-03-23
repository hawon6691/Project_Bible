package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.ApiEnvelope
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.ApiErrorPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requestId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.json.JsonElement
import java.time.Instant

class PbShopException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
) : RuntimeException(message)

data class StubResponse(
    val status: HttpStatusCode = HttpStatusCode.OK,
    val data: Any?,
    val meta: Any? = null,
)

suspend fun ApplicationCall.respondSuccess(
    status: HttpStatusCode = HttpStatusCode.OK,
    data: Any?,
    meta: Any? = null,
) {
    respond(
        status,
        ApiEnvelope<JsonElement>(
            success = true,
            data = data.toJsonElement(),
            meta = meta?.toJsonElement(),
            requestId = requestId(),
            timestamp = Instant.now().toString(),
        ),
    )
}

suspend fun ApplicationCall.respondStub(stubResponse: StubResponse) {
    respondSuccess(
        status = stubResponse.status,
        data = stubResponse.data,
        meta = stubResponse.meta,
    )
}

suspend fun ApplicationCall.respondFailure(
    status: HttpStatusCode,
    code: String,
    message: String,
) {
    respond(
        status,
        ApiEnvelope<JsonElement>(
            success = false,
            error = ApiErrorPayload(code = code, message = message, path = request.path()),
            requestId = requestId(),
            timestamp = Instant.now().toString(),
        ),
    )
}
