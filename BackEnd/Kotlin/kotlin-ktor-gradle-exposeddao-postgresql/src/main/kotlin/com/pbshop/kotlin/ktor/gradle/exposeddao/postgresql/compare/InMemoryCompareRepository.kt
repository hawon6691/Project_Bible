package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

class InMemoryCompareRepository(
    private val products: Map<Int, CompareDetailItemRecord>,
    seededItems: Map<String, Set<Int>> = emptyMap(),
) : CompareRepository {
    private val itemsByKey = seededItems.mapValues { it.value.toMutableSet() }.toMutableMap()

    override fun productExists(productId: Int): Boolean = products.containsKey(productId)

    override fun add(compareKey: String, productId: Int) {
        itemsByKey.getOrPut(compareKey) { linkedSetOf() }.add(productId)
    }

    override fun remove(compareKey: String, productId: Int) {
        itemsByKey[compareKey]?.remove(productId)
    }

    override fun list(compareKey: String): List<CompareItemRecord> =
        itemsByKey[compareKey].orEmpty().mapNotNull { id ->
            products[id]?.let {
                CompareItemRecord(it.productId, it.name, it.slug, "/images/products/${it.productId}.jpg")
            }
        }

    override fun detail(compareKey: String): List<CompareDetailItemRecord> =
        itemsByKey[compareKey].orEmpty().mapNotNull(products::get)

    companion object {
        fun seeded(): InMemoryCompareRepository =
            InMemoryCompareRepository(
                products =
                    listOf(
                        CompareDetailItemRecord(1, "게이밍 노트북 A15", "gaming-notebook-a15", 2, 1720000, 4.5, mapOf("CPU" to "Intel i7-14700H", "RAM" to "16GB", "SSD" to "1TB")),
                        CompareDetailItemRecord(2, "사무용 노트북 Slim", "office-notebook-slim", 2, 940000, 4.0, mapOf("CPU" to "Intel i5-13420H", "RAM" to "16GB", "SSD" to "512GB")),
                        CompareDetailItemRecord(3, "미니 데스크탑 Pro", "mini-desktop-pro", 3, 1360000, 5.0, mapOf("CPU" to "Ryzen 7", "RAM" to "32GB", "GPU" to "RTX 4060")),
                    ).associateBy { it.productId },
                seededItems = mapOf("guest" to linkedSetOf(1, 2)),
            )
    }
}
