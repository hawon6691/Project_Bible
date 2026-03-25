package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

class InMemorySpecRepository(
    seededDefinitions: List<SpecDefinitionRecord> = emptyList(),
    seededProductSpecs: Map<Int, List<ProductSpecRecord>> = emptyMap(),
    seededSpecScores: Map<Int, List<SpecScoreRecord>> = emptyMap(),
    seededProducts: Map<Int, String> = emptyMap(),
    seededCategories: Map<Int, String> = emptyMap(),
) : SpecRepository {
    private val definitions = linkedMapOf<Int, SpecDefinitionRecord>()
    private val productSpecs = seededProductSpecs.mapValues { it.value.toMutableList() }.toMutableMap()
    private val specScores = seededSpecScores.mapValues { it.value.toMutableList() }.toMutableMap()
    private val products = seededProducts.toMutableMap()
    private val categories = seededCategories.toMutableMap()
    private var nextDefinitionId = 1
    private var nextScoreId = 1

    init {
        seededDefinitions.forEach {
            definitions[it.id] = it
            nextDefinitionId = maxOf(nextDefinitionId, it.id + 1)
        }
        seededSpecScores.values.flatten().forEach {
            nextScoreId = maxOf(nextScoreId, it.id + 1)
        }
    }

    override fun listDefinitions(categoryId: Int?): List<SpecDefinitionRecord> =
        definitions.values
            .filter { categoryId == null || it.categoryId == categoryId }
            .sortedWith(compareBy<SpecDefinitionRecord> { it.categoryId }.thenBy { it.sortOrder }.thenBy { it.id })

    override fun findDefinitionById(id: Int): SpecDefinitionRecord? = definitions[id]

    override fun createDefinition(newDefinition: NewSpecDefinition): SpecDefinitionRecord {
        val created =
            SpecDefinitionRecord(
                id = nextDefinitionId++,
                categoryId = newDefinition.categoryId,
                categoryName = categories[newDefinition.categoryId],
                name = newDefinition.name,
                type = newDefinition.type,
                options = newDefinition.options,
                unit = newDefinition.unit,
                isComparable = newDefinition.isComparable,
                dataType = newDefinition.dataType,
                sortOrder = newDefinition.sortOrder,
            )
        definitions[created.id] = created
        return created
    }

    override fun updateDefinition(
        id: Int,
        update: SpecDefinitionUpdate,
    ): SpecDefinitionRecord {
        val current = requireNotNull(definitions[id]) { "Spec definition $id not found" }
        val updated =
            current.copy(
                categoryId = update.categoryId ?: current.categoryId,
                categoryName = categories[update.categoryId ?: current.categoryId],
                name = update.name ?: current.name,
                type = update.type ?: current.type,
                options = update.options ?: current.options,
                unit = update.unit ?: current.unit,
                isComparable = update.isComparable ?: current.isComparable,
                dataType = update.dataType ?: current.dataType,
                sortOrder = update.sortOrder ?: current.sortOrder,
            )
        definitions[id] = updated
        return updated
    }

    override fun deleteDefinition(id: Int) {
        definitions.remove(id) ?: error("Spec definition $id not found")
        specScores.remove(id)
    }

    override fun definitionInUse(id: Int): Boolean =
        productSpecs.values.flatten().any { it.specDefinitionId == id }

    override fun categoryExists(categoryId: Int): Boolean = categories.containsKey(categoryId)

    override fun productExists(productId: Int): Boolean = products.containsKey(productId)

    override fun listProductSpecs(productId: Int): List<ProductSpecRecord> =
        productSpecs[productId].orEmpty().sortedBy { it.specDefinitionId }

    override fun replaceProductSpecs(
        productId: Int,
        values: List<NewProductSpecValue>,
    ): List<ProductSpecRecord> {
        val replaced =
            values.map { value ->
                val definition = requireNotNull(definitions[value.specDefinitionId]) { "Spec definition ${value.specDefinitionId} not found" }
                ProductSpecRecord(
                    specDefinitionId = value.specDefinitionId,
                    name = definition.name,
                    value = value.value,
                    numericValue = value.numericValue,
                )
            }
        productSpecs[productId] = replaced.toMutableList()
        return replaced
    }

    override fun listComparedProducts(productIds: List<Int>): List<ComparedProductRecord> =
        productIds.mapNotNull { productId ->
            products[productId]?.let { name ->
                ComparedProductRecord(
                    productId = productId,
                    productName = name,
                    specs = listProductSpecs(productId),
                )
            }
        }

    override fun listSpecScores(specDefinitionId: Int): List<SpecScoreRecord> =
        specScores[specDefinitionId].orEmpty().sortedBy { it.id }

    override fun replaceSpecScores(
        specDefinitionId: Int,
        values: List<NewSpecScore>,
    ): List<SpecScoreRecord> {
        val replaced =
            values.map { value ->
                SpecScoreRecord(
                    id = nextScoreId++,
                    specDefinitionId = specDefinitionId,
                    value = value.value,
                    score = value.score,
                    benchmarkSource = value.benchmarkSource,
                )
            }
        specScores[specDefinitionId] = replaced.toMutableList()
        return replaced
    }

    companion object {
        fun seeded(): InMemorySpecRepository =
            InMemorySpecRepository(
                seededDefinitions =
                    listOf(
                        SpecDefinitionRecord(1, 2, "노트북", "CPU", "TEXT", null, null, true, "STRING", 1),
                        SpecDefinitionRecord(2, 2, "노트북", "RAM", "NUMBER", null, "GB", true, "NUMBER", 2),
                        SpecDefinitionRecord(3, 2, "노트북", "SSD", "NUMBER", null, "GB", true, "NUMBER", 3),
                        SpecDefinitionRecord(4, 3, "데스크탑", "GPU", "TEXT", null, null, true, "STRING", 1),
                        SpecDefinitionRecord(5, 5, "전기차", "주행거리", "NUMBER", null, "km", true, "NUMBER", 1),
                    ),
                seededProductSpecs =
                    mapOf(
                        1 to listOf(ProductSpecRecord(1, "CPU", "Intel i7-14700H", null), ProductSpecRecord(2, "RAM", "16", 16.0), ProductSpecRecord(3, "SSD", "1024", 1024.0)),
                        2 to listOf(ProductSpecRecord(1, "CPU", "Intel i5-13420H", null), ProductSpecRecord(2, "RAM", "16", 16.0)),
                        3 to listOf(ProductSpecRecord(4, "GPU", "RTX 4060", null)),
                        5 to listOf(ProductSpecRecord(5, "주행거리", "520", 520.0)),
                    ),
                seededSpecScores =
                    mapOf(
                        2 to listOf(SpecScoreRecord(1, 2, "8", 55, "internal"), SpecScoreRecord(2, 2, "16", 82, "internal"), SpecScoreRecord(3, 2, "32", 95, "internal")),
                        3 to listOf(SpecScoreRecord(4, 3, "512", 70, "internal"), SpecScoreRecord(5, 3, "1024", 90, "internal")),
                    ),
                seededProducts = mapOf(1 to "게이밍 노트북 A15", 2 to "사무용 노트북 Slim", 3 to "미니 데스크탑 Pro", 5 to "전기차 EV 520"),
                seededCategories = mapOf(1 to "컴퓨터", 2 to "노트북", 3 to "데스크탑", 4 to "자동차", 5 to "전기차"),
            )
    }
}
