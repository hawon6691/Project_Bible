package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common

open class StubDomainRepository(
    private val operations: List<StubOperation>,
) {
    val specs: List<EndpointSpec> = operations.map { it.spec }

    fun execute(
        endpointKey: String,
        request: EndpointRequest,
    ): EndpointResponse =
        operations
            .firstOrNull { it.spec.key == endpointKey }
            ?.handler
            ?.invoke(request)
            ?: error("No stub operation registered for $endpointKey")
}

open class StubDomainService(
    private val repository: StubDomainRepository,
) {
    val specs: List<EndpointSpec>
        get() = repository.specs

    fun execute(
        endpointKey: String,
        request: EndpointRequest,
    ): EndpointResponse = repository.execute(endpointKey, request)
}

fun List<StubOperation>.filterByTags(vararg tags: String): List<StubOperation> {
    val tagSet = tags.toSet()
    return filter { it.spec.tag in tagSet }
}
