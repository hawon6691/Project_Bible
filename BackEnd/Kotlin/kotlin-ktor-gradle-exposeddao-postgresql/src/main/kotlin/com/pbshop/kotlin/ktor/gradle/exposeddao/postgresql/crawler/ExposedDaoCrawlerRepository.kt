package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CrawlerJobsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CrawlerLogStatus
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CrawlerLogsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoCrawlerRepository(
    private val databaseFactory: DatabaseFactory,
) : CrawlerRepository {
    override fun listJobs(
        status: String?,
        page: Int,
        limit: Int,
    ): CrawlerJobListResult =
        databaseFactory.withTransaction {
            val filtered =
                CrawlerJobsTable.innerJoin(SellersTable)
                    .selectAll()
                    .orderBy(CrawlerJobsTable.id to SortOrder.ASC)
                    .map(::toJobRecord)
                    .filter { status == null || it.latestStatus.equals(status, true) }
            val offset = (page - 1) * limit
            CrawlerJobListResult(filtered.drop(offset).take(limit), filtered.size)
        }

    override fun findJobById(id: Int): CrawlerJobRecord? =
        databaseFactory.withTransaction {
            CrawlerJobsTable.innerJoin(SellersTable)
                .selectAll()
                .where { CrawlerJobsTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.let(::toJobRecord)
        }

    override fun createJob(newJob: NewCrawlerJob): CrawlerJobRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val id =
                CrawlerJobsTable.insertAndGetId { row ->
                    row[seller] = EntityID(newJob.sellerId, SellersTable)
                    row[name] = newJob.name
                    row[cronExpression] = newJob.cronExpression
                    row[collectPrice] = newJob.collectPrice
                    row[collectSpec] = newJob.collectSpec
                    row[detectAnomaly] = newJob.detectAnomaly
                    row[isActive] = newJob.isActive
                    row[lastTriggeredAt] = null
                    row[createdAt] = now
                    row[updatedAt] = now
                }.value
            requireNotNull(findJobById(id))
        }

    override fun updateJob(
        id: Int,
        update: CrawlerJobUpdate,
    ): CrawlerJobRecord =
        databaseFactory.withTransaction {
            val current = requireNotNull(findJobById(id))
            CrawlerJobsTable.update({ CrawlerJobsTable.id eq id }) { row ->
                row[seller] = EntityID(update.sellerId ?: current.sellerId, SellersTable)
                row[name] = update.name ?: current.name
                row[cronExpression] = update.cronExpression ?: current.cronExpression
                row[collectPrice] = update.collectPrice ?: current.collectPrice
                row[collectSpec] = update.collectSpec ?: current.collectSpec
                row[detectAnomaly] = update.detectAnomaly ?: current.detectAnomaly
                row[isActive] = update.isActive ?: current.isActive
                row[updatedAt] = Instant.now()
            }
            requireNotNull(findJobById(id))
        }

    override fun deleteJob(id: Int) {
        databaseFactory.withTransaction {
            CrawlerLogsTable.deleteWhere { CrawlerLogsTable.job eq id }
            CrawlerJobsTable.deleteWhere { CrawlerJobsTable.id eq id }
        }
    }

    override fun triggerJob(id: Int): Pair<String, Long> =
        databaseFactory.withTransaction {
            val now = Instant.now()
            CrawlerJobsTable.update({ CrawlerJobsTable.id eq id }) {
                it[lastTriggeredAt] = now
                it[updatedAt] = now
            }
            val runId =
                CrawlerLogsTable.insertAndGetId { row ->
                    row[job] = EntityID(id, CrawlerJobsTable)
                    row[status] = CrawlerLogStatus.SUCCESS
                    row[startedAt] = now.minusSeconds(2)
                    row[finishedAt] = now
                    row[durationMs] = 2000
                    row[itemsProcessed] = 10
                    row[itemsCreated] = 1
                    row[itemsUpdated] = 3
                    row[itemsFailed] = 0
                    row[errorMessage] = null
                    row[createdAt] = now
                }.value
            "Crawler job queued." to runId
        }

    override fun triggerManual(
        jobId: Int?,
        sellerId: Int?,
        name: String?,
    ): Pair<String, Long> =
        databaseFactory.withTransaction {
            val targetId =
                jobId
                    ?: CrawlerJobsTable.selectAll()
                        .let { rows ->
                            rows.firstOrNull {
                                sellerId == null || it[CrawlerJobsTable.seller].value == sellerId
                            }?.get(CrawlerJobsTable.id)?.value
                        }
                    ?: throw IllegalArgumentException("No crawler job available")
            triggerJob(targetId)
        }

    override fun listRuns(
        status: String?,
        jobId: Int?,
        page: Int,
        limit: Int,
    ): CrawlerRunListResult =
        databaseFactory.withTransaction {
            val filtered =
                CrawlerLogsTable.innerJoin(CrawlerJobsTable)
                    .selectAll()
                    .orderBy(CrawlerLogsTable.startedAt to SortOrder.DESC)
                    .map(::toRunRecord)
                    .filter {
                        (status == null || it.status.equals(status, true)) &&
                            (jobId == null || it.jobId == jobId)
                    }
            val offset = (page - 1) * limit
            CrawlerRunListResult(filtered.drop(offset).take(limit), filtered.size)
        }

    override fun monitoring(): CrawlerMonitoringRecord =
        databaseFactory.withTransaction {
            val runs = listRuns(null, null, 1, Int.MAX_VALUE).items
            val successRuns = runs.filter { it.status.equals("SUCCESS", true) }
            CrawlerMonitoringRecord(
                totalJobs = CrawlerJobsTable.selectAll().count().toInt(),
                activeJobs = CrawlerJobsTable.selectAll().where { CrawlerJobsTable.isActive eq true }.count().toInt(),
                recentRunCount = runs.size,
                successCount = successRuns.size,
                failedCount = runs.count { it.status.equals("FAILED", true) },
                lastSuccessAt = successRuns.maxByOrNull { it.finishedAt ?: it.startedAt }?.finishedAt,
            )
        }

    override fun sellerExists(sellerId: Int): Boolean =
        databaseFactory.withTransaction {
            SellersTable.selectAll().where { SellersTable.id eq sellerId }.limit(1).any()
        }

    private fun toJobRecord(row: ResultRow): CrawlerJobRecord {
        val jobId = row[CrawlerJobsTable.id].value
        val latestStatus =
            CrawlerLogsTable.selectAll()
                .where { CrawlerLogsTable.job eq jobId }
                .orderBy(CrawlerLogsTable.startedAt to SortOrder.DESC)
                .limit(1)
                .firstOrNull()
                ?.get(CrawlerLogsTable.status)
                ?.name
                ?: "IDLE"
        return CrawlerJobRecord(
            id = jobId,
            sellerId = row[CrawlerJobsTable.seller].value,
            sellerName = row[SellersTable.name],
            name = row[CrawlerJobsTable.name],
            cronExpression = row[CrawlerJobsTable.cronExpression],
            collectPrice = row[CrawlerJobsTable.collectPrice],
            collectSpec = row[CrawlerJobsTable.collectSpec],
            detectAnomaly = row[CrawlerJobsTable.detectAnomaly],
            isActive = row[CrawlerJobsTable.isActive],
            lastTriggeredAt = row[CrawlerJobsTable.lastTriggeredAt],
            latestStatus = latestStatus,
            createdAt = row[CrawlerJobsTable.createdAt],
            updatedAt = row[CrawlerJobsTable.updatedAt],
        )
    }

    private fun toRunRecord(row: ResultRow): CrawlerRunRecord =
        CrawlerRunRecord(
            id = row[CrawlerLogsTable.id].value,
            jobId = row[CrawlerLogsTable.job].value,
            jobName = row[CrawlerJobsTable.name],
            status = row[CrawlerLogsTable.status].name,
            startedAt = row[CrawlerLogsTable.startedAt],
            finishedAt = row[CrawlerLogsTable.finishedAt],
            durationMs = row[CrawlerLogsTable.durationMs],
            itemsProcessed = row[CrawlerLogsTable.itemsProcessed],
            itemsCreated = row[CrawlerLogsTable.itemsCreated],
            itemsUpdated = row[CrawlerLogsTable.itemsUpdated],
            itemsFailed = row[CrawlerLogsTable.itemsFailed],
            errorMessage = row[CrawlerLogsTable.errorMessage],
        )
}
