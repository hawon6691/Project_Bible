package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class AutoService(
    private val repository: AutoRepository,
) {
    fun models(brand: String?, type: String?): StubResponse =
        StubResponse(
            data =
                repository.listModels(brand?.trim(), type?.trim()).map {
                    mapOf(
                        "id" to it.id,
                        "brand" to it.brand,
                        "name" to it.name,
                        "type" to it.type,
                        "year" to it.year,
                        "basePrice" to it.basePrice,
                        "imageUrl" to it.imageUrl,
                    )
                },
        )

    fun trims(modelId: Int): StubResponse {
        val model = requireModel(modelId)
        val trims = repository.listTrims(modelId)
        val optionsByTrim = repository.listOptionsForTrimIds(trims.map { it.id }).groupBy { it.trimId }
        return StubResponse(
            data =
                trims.map { trim ->
                    mapOf(
                        "id" to trim.id,
                        "modelId" to trim.modelId,
                        "name" to trim.name,
                        "basePrice" to trim.basePrice,
                        "options" to optionsByTrim[trim.id].orEmpty().map(::optionPayload),
                        "model" to mapOf("id" to model.id, "brand" to model.brand, "name" to model.name),
                    )
                },
        )
    }

    fun estimate(request: AutoEstimateRequest): StubResponse {
        val model = requireModel(request.modelId)
        val trim =
            repository.listTrims(model.id).firstOrNull { it.id == request.trimId }
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "해당 모델의 트림을 찾을 수 없습니다.")
        val optionsById = repository.listOptionsForTrimIds(listOf(trim.id)).associateBy { it.id }
        val selectedOptions =
            request.optionIds.distinct().map { optionId ->
                optionsById[optionId]
                    ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "지원하지 않는 옵션이 포함되어 있습니다.")
            }
        val basePrice = trim.basePrice
        val optionPrice = selectedOptions.sumOf { it.price }
        val tax = ((basePrice + optionPrice) * 0.07).toInt()
        val totalPrice = basePrice + optionPrice + tax
        val monthlyPayment = ((totalPrice - (totalPrice * 0.1)) / 60.0).toInt()
        return StubResponse(
            status = HttpStatusCode.Created,
            data =
                mapOf(
                    "modelId" to model.id,
                    "trimId" to trim.id,
                    "selectedOptionIds" to selectedOptions.map { it.id },
                    "basePrice" to basePrice,
                    "optionPrice" to optionPrice,
                    "tax" to tax,
                    "totalPrice" to totalPrice,
                    "monthlyPayment" to monthlyPayment,
                ),
        )
    }

    fun leaseOffers(modelId: Int): StubResponse {
        requireModel(modelId)
        return StubResponse(
            data =
                repository.listLeaseOffers(modelId)
                    .filter { it.isActive }
                    .map {
                        mapOf(
                            "id" to it.id,
                            "modelId" to it.modelId,
                            "company" to it.company,
                            "type" to it.type,
                            "monthlyPayment" to it.monthlyPayment,
                            "deposit" to it.deposit,
                            "contractMonths" to it.contractMonths,
                            "annualMileage" to it.annualMileage,
                        )
                    },
        )
    }

    private fun requireModel(modelId: Int): AutoModelRecord =
        repository.findModelById(modelId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "자동차 모델을 찾을 수 없습니다.")

    private fun optionPayload(option: AutoOptionRecord): Map<String, Any> =
        mapOf(
            "id" to option.id,
            "trimId" to option.trimId,
            "name" to option.name,
            "price" to option.price,
        )
}
