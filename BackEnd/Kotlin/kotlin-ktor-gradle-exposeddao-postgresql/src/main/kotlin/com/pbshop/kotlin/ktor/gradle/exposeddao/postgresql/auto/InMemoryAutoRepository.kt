package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

class InMemoryAutoRepository(
    private val models: MutableList<AutoModelRecord>,
    private val trims: MutableList<AutoTrimRecord>,
    private val options: MutableList<AutoOptionRecord>,
    private val offers: MutableList<LeaseOfferRecord>,
) : AutoRepository {
    override fun listModels(brand: String?, type: String?): List<AutoModelRecord> =
        models.filter { model ->
            model.isActive &&
                (brand.isNullOrBlank() || model.brand.equals(brand, ignoreCase = true)) &&
                (type.isNullOrBlank() || model.type.equals(type, ignoreCase = true))
        }

    override fun findModelById(modelId: Int): AutoModelRecord? = models.firstOrNull { it.id == modelId && it.isActive }

    override fun listTrims(modelId: Int): List<AutoTrimRecord> = trims.filter { it.modelId == modelId }.sortedBy { it.id }

    override fun listOptionsForTrimIds(trimIds: List<Int>): List<AutoOptionRecord> = options.filter { it.trimId in trimIds }.sortedBy { it.id }

    override fun listLeaseOffers(modelId: Int): List<LeaseOfferRecord> = offers.filter { it.modelId == modelId }.sortedBy { it.id }

    companion object {
        fun seeded(): InMemoryAutoRepository =
            InMemoryAutoRepository(
                models =
                    mutableListOf(
                        AutoModelRecord(1, "Hyundai", "IONIQ 6", "EV", 2025, 52000000, "/uploads/auto/ioniq6.jpg", true),
                        AutoModelRecord(2, "Kia", "Sorento", "SUV", 2025, 42000000, "/uploads/auto/sorento.jpg", true),
                        AutoModelRecord(3, "Genesis", "G80", "SEDAN", 2025, 61000000, "/uploads/auto/g80.jpg", true),
                    ),
                trims =
                    mutableListOf(
                        AutoTrimRecord(1, 1, "E-LITE", 52000000),
                        AutoTrimRecord(2, 1, "E-PLUS", 56500000),
                        AutoTrimRecord(3, 2, "2.2 Diesel", 42000000),
                        AutoTrimRecord(4, 2, "Hybrid", 45800000),
                        AutoTrimRecord(5, 3, "2.5 Turbo", 61000000),
                    ),
                options =
                    mutableListOf(
                        AutoOptionRecord(1, 1, "Heat Pump", 1200000),
                        AutoOptionRecord(2, 2, "HUD Package", 1800000),
                        AutoOptionRecord(3, 3, "Drive Wise", 1500000),
                        AutoOptionRecord(4, 4, "Panorama Sunroof", 900000),
                        AutoOptionRecord(5, 5, "Lexicon Sound", 1600000),
                    ),
                offers =
                    mutableListOf(
                        LeaseOfferRecord(1, 1, "LeaseOne", "LEASE", 790000, 5000000, 48, 20000, true),
                        LeaseOfferRecord(2, 1, "RentCarX", "RENT", 730000, 3000000, 60, 20000, true),
                        LeaseOfferRecord(3, 2, "LeaseOne", "LEASE", 620000, 4000000, 48, 18000, true),
                        LeaseOfferRecord(4, 3, "PremiumLease", "LEASE", 980000, 7000000, 48, 15000, true),
                    ),
            )
    }
}
