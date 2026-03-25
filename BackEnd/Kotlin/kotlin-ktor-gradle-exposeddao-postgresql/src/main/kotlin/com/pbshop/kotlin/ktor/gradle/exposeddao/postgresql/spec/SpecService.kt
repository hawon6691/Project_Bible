package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class SpecService(
    private val repository: SpecRepository,
) {
    fun listDefinitions(categoryId: Int?): StubResponse =
        StubResponse(data = repository.listDefinitions(categoryId).map(::definitionPayload))

    fun createDefinition(request: SpecDefinitionRequest): StubResponse {
        validateDefinitionRequest(request)
        ensureCategoryExists(request.categoryId)
        val created =
            repository.createDefinition(
                NewSpecDefinition(
                    categoryId = request.categoryId,
                    name = request.name.trim(),
                    type = normalizeEnum(request.type, SPEC_TYPES, "유효하지 않은 스펙 입력 타입입니다."),
                    options = request.options?.map(String::trim)?.filter(String::isNotBlank)?.takeIf { it.isNotEmpty() },
                    unit = request.unit?.trim()?.takeIf { it.isNotBlank() },
                    isComparable = request.isComparable,
                    dataType = normalizeEnum(request.dataType, SPEC_DATA_TYPES, "유효하지 않은 스펙 데이터 타입입니다."),
                    sortOrder = request.sortOrder.coerceAtLeast(0),
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = definitionPayload(created))
    }

    fun updateDefinition(
        id: Int,
        request: SpecDefinitionUpdateRequest,
    ): StubResponse {
        if (
            request.categoryId == null &&
            request.name == null &&
            request.type == null &&
            request.options == null &&
            request.unit == null &&
            request.isComparable == null &&
            request.dataType == null &&
            request.sortOrder == null
        ) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 스펙 정의 값이 없습니다.")
        }
        request.categoryId?.let(::ensureCategoryExists)
        request.name?.let(::validateDefinitionName)
        request.type?.let { normalizeEnum(it, SPEC_TYPES, "유효하지 않은 스펙 입력 타입입니다.") }
        request.dataType?.let { normalizeEnum(it, SPEC_DATA_TYPES, "유효하지 않은 스펙 데이터 타입입니다.") }
        request.sortOrder?.takeIf { it < 0 }?.let {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "sortOrder는 0 이상이어야 합니다.")
        }

        val updated =
            repository.updateDefinition(
                id,
                SpecDefinitionUpdate(
                    categoryId = request.categoryId,
                    name = request.name?.trim(),
                    type = request.type?.let { normalizeEnum(it, SPEC_TYPES, "유효하지 않은 스펙 입력 타입입니다.") },
                    options = request.options?.map(String::trim)?.filter(String::isNotBlank),
                    unit = request.unit?.trim(),
                    isComparable = request.isComparable,
                    dataType = request.dataType?.let { normalizeEnum(it, SPEC_DATA_TYPES, "유효하지 않은 스펙 데이터 타입입니다.") },
                    sortOrder = request.sortOrder?.coerceAtLeast(0),
                ),
            )
        return StubResponse(data = definitionPayload(updated))
    }

    fun deleteDefinition(id: Int): StubResponse {
        if (repository.definitionInUse(id)) {
            throw PbShopException(HttpStatusCode.Conflict, "SPEC_DEFINITION_IN_USE", "사용 중인 스펙 정의는 삭제할 수 없습니다.")
        }
        repository.deleteDefinition(id)
        return StubResponse(data = mapOf("message" to "스펙 정의가 삭제되었습니다."))
    }

    fun listProductSpecs(productId: Int): StubResponse {
        ensureProductExists(productId)
        return StubResponse(data = repository.listProductSpecs(productId).map(::productSpecPayload))
    }

    fun replaceProductSpecs(
        productId: Int,
        request: List<ProductSpecValueRequest>,
    ): StubResponse {
        ensureProductExists(productId)
        if (request.isEmpty()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "최소 1개 이상의 상품 스펙 값이 필요합니다.")
        }
        val values =
            request.map { value ->
                if (value.value.trim().isBlank()) {
                    throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "스펙 값은 비어 있을 수 없습니다.")
                }
                val definition =
                    repository.findDefinitionById(value.specDefinitionId)
                        ?: throw PbShopException(HttpStatusCode.BadRequest, "SPEC_DEFINITION_NOT_FOUND", "존재하지 않는 스펙 정의입니다.")
                if (definition.dataType == "NUMBER" && value.numericValue == null) {
                    throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "NUMBER 스펙은 numericValue가 필요합니다.")
                }
                NewProductSpecValue(
                    specDefinitionId = value.specDefinitionId,
                    value = value.value.trim(),
                    numericValue = value.numericValue,
                )
            }
        return StubResponse(data = repository.replaceProductSpecs(productId, values).map(::productSpecPayload))
    }

    fun compare(request: SpecCompareRequest): StubResponse {
        val products = requireComparedProducts(request.productIds)
        val diff =
            products.flatMap { it.specs.map(ProductSpecRecord::name) }
                .distinct()
                .filter { specName ->
                    products.map { product -> product.specs.firstOrNull { it.name == specName }?.value }.distinct().size > 1
                }

        return StubResponse(
            data =
                mapOf(
                    "items" to products.map(::comparedProductPayload),
                    "diff" to diff,
                ),
        )
    }

    fun compareScored(request: SpecCompareRequest): StubResponse {
        val products = requireComparedProducts(request.productIds)
        val scoreMap =
            products.flatMap { it.specs }
                .map { it.specDefinitionId }
                .distinct()
                .associateWith { repository.listSpecScores(it) }

        val scores =
            products.map { product ->
                val total =
                    product.specs.sumOf { spec ->
                        val score = scoreMap[spec.specDefinitionId].orEmpty().firstOrNull { it.value == spec.value }?.score
                        score ?: spec.numericValue?.toInt() ?: 0
                    }
                mapOf("productId" to product.productId, "score" to total)
            }

        val winnerProductId = scores.maxByOrNull { it["score"] as Int }?.get("productId")

        return StubResponse(
            data =
                mapOf(
                    "winnerProductId" to winnerProductId,
                    "scores" to scores,
                    "items" to products.map(::comparedProductPayload),
                ),
        )
    }

    fun replaceSpecScores(
        specDefinitionId: Int,
        request: List<SpecScoreRequest>,
    ): StubResponse {
        repository.findDefinitionById(specDefinitionId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "SPEC_DEFINITION_NOT_FOUND", "존재하지 않는 스펙 정의입니다.")
        if (request.isEmpty()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "최소 1개 이상의 점수 매핑이 필요합니다.")
        }
        val values =
            request.map {
                if (it.value.trim().isBlank()) {
                    throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "점수 매핑 값은 비어 있을 수 없습니다.")
                }
                if (it.score < 0) {
                    throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "점수는 0 이상이어야 합니다.")
                }
                NewSpecScore(it.value.trim(), it.score, it.benchmarkSource?.trim()?.takeIf(String::isNotBlank))
            }
        return StubResponse(data = repository.replaceSpecScores(specDefinitionId, values).map(::scorePayload))
    }

    private fun requireComparedProducts(productIds: List<Int>): List<ComparedProductRecord> {
        val normalized = productIds.distinct()
        if (normalized.size < 2) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "비교할 상품은 최소 2개 이상 필요합니다.")
        }
        val products = repository.listComparedProducts(normalized)
        if (products.size < 2) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "비교 대상 상품을 찾을 수 없습니다.")
        }
        return products
    }

    private fun ensureCategoryExists(categoryId: Int) {
        if (!repository.categoryExists(categoryId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.")
        }
    }

    private fun ensureProductExists(productId: Int) {
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
    }

    private fun validateDefinitionRequest(request: SpecDefinitionRequest) {
        validateDefinitionName(request.name)
        normalizeEnum(request.type, SPEC_TYPES, "유효하지 않은 스펙 입력 타입입니다.")
        normalizeEnum(request.dataType, SPEC_DATA_TYPES, "유효하지 않은 스펙 데이터 타입입니다.")
        if (request.sortOrder < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "sortOrder는 0 이상이어야 합니다.")
        }
    }

    private fun validateDefinitionName(name: String) {
        val normalized = name.trim()
        if (normalized.isBlank() || normalized.length > 50) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "스펙 정의명은 1자 이상 50자 이하로 입력해주세요.")
        }
    }

    private fun normalizeEnum(
        value: String,
        candidates: Set<String>,
        message: String,
    ): String {
        val normalized = value.trim().uppercase()
        if (normalized !in candidates) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", message)
        }
        return normalized
    }

    private fun definitionPayload(record: SpecDefinitionRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "categoryId" to record.categoryId,
            "categoryName" to record.categoryName,
            "name" to record.name,
            "type" to record.type,
            "options" to record.options,
            "unit" to record.unit,
            "isComparable" to record.isComparable,
            "dataType" to record.dataType,
            "sortOrder" to record.sortOrder,
        )

    private fun productSpecPayload(record: ProductSpecRecord): Map<String, Any?> =
        mapOf(
            "specDefinitionId" to record.specDefinitionId,
            "name" to record.name,
            "value" to record.value,
            "numericValue" to record.numericValue,
        )

    private fun comparedProductPayload(record: ComparedProductRecord): Map<String, Any?> =
        mapOf(
            "productId" to record.productId,
            "name" to record.productName,
            "specs" to record.specs.map(::productSpecPayload),
        )

    private fun scorePayload(record: SpecScoreRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "specDefinitionId" to record.specDefinitionId,
            "value" to record.value,
            "score" to record.score,
            "benchmarkSource" to record.benchmarkSource,
        )

    companion object {
        private val SPEC_TYPES = setOf("TEXT", "NUMBER", "SELECT")
        private val SPEC_DATA_TYPES = setOf("STRING", "NUMBER", "BOOLEAN")
    }
}
