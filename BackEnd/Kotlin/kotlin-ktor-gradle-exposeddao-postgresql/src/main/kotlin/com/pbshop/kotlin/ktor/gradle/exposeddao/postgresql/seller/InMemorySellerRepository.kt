package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

import java.time.Instant

class InMemorySellerRepository(
    seededSellers: List<SellerRecord> = emptyList(),
    seededLinkedSellerIds: Set<Int> = emptySet(),
) : SellerRepository {
    private val sellers = linkedMapOf<Int, SellerRecord>()
    private val linkedSellerIds = seededLinkedSellerIds.toMutableSet()
    private var nextId = 1

    init {
        seededSellers.forEach {
            sellers[it.id] = it
            nextId = maxOf(nextId, it.id + 1)
        }
    }

    override fun listSellers(
        page: Int,
        limit: Int,
    ): SellerListResult {
        val items = sellers.values.sortedBy { it.id }
        val offset = (page - 1) * limit
        return SellerListResult(items.drop(offset).take(limit), items.size)
    }

    override fun findSellerById(id: Int): SellerRecord? = sellers[id]

    override fun createSeller(newSeller: NewSeller): SellerRecord {
        val created =
            SellerRecord(
                id = nextId++,
                name = newSeller.name,
                url = newSeller.url,
                logoUrl = newSeller.logoUrl,
                trustScore = newSeller.trustScore,
                trustGrade = newSeller.trustGrade,
                description = newSeller.description,
                isActive = newSeller.isActive,
                createdAt = Instant.now(),
            )
        sellers[created.id] = created
        return created
    }

    override fun updateSeller(
        id: Int,
        update: SellerUpdate,
    ): SellerRecord {
        val current = requireNotNull(sellers[id]) { "Seller $id not found" }
        val updated =
            current.copy(
                name = update.name ?: current.name,
                url = update.url ?: current.url,
                logoUrl = update.logoUrl ?: current.logoUrl,
                trustScore = update.trustScore ?: current.trustScore,
                trustGrade = update.trustGrade ?: current.trustGrade,
                description = update.description ?: current.description,
                isActive = update.isActive ?: current.isActive,
            )
        sellers[id] = updated
        return updated
    }

    override fun deleteSeller(id: Int) {
        sellers.remove(id) ?: error("Seller $id not found")
        linkedSellerIds.remove(id)
    }

    override fun hasLinkedPriceEntries(id: Int): Boolean = linkedSellerIds.contains(id)

    companion object {
        fun seeded(): InMemorySellerRepository {
            val now = Instant.now()
            return InMemorySellerRepository(
                seededSellers =
                    listOf(
                        SellerRecord(1, "공식몰", "https://official.example.com", "https://img.example.com/s1-logo.png", 92, "A", "제조사 공식 판매처", true, now.minusSeconds(864000)),
                        SellerRecord(2, "테크마켓", "https://techmarket.example.com", "https://img.example.com/s2-logo.png", 84, "B", "IT 전문 판매처", true, now.minusSeconds(840000)),
                        SellerRecord(3, "카월드", "https://carworld.example.com", "https://img.example.com/s3-logo.png", 79, "B", "자동차 전문 판매처", true, now.minusSeconds(820000)),
                    ),
                seededLinkedSellerIds = setOf(1, 2, 3),
            )
        }
    }
}
