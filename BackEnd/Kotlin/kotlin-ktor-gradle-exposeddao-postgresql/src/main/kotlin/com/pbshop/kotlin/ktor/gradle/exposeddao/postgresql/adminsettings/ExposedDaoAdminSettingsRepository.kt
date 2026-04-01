package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SystemSettingsTable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoAdminSettingsRepository(
    private val databaseFactory: DatabaseFactory,
) : AdminSettingsRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllowedExtensions(): AllowedExtensionsRecord = getOrCreate(ALLOWED_EXTENSIONS_KEY, DEFAULT_EXTENSIONS)

    override fun setAllowedExtensions(extensions: List<String>): AllowedExtensionsRecord =
        save(ALLOWED_EXTENSIONS_KEY, AllowedExtensionsRecord(extensions))

    override fun getUploadLimits(): UploadLimitsRecord = getOrCreate(UPLOAD_LIMITS_KEY, DEFAULT_UPLOAD_LIMITS)

    override fun updateUploadLimits(image: Int?, video: Int?, audio: Int?): UploadLimitsRecord {
        val current = getUploadLimits()
        return save(
            UPLOAD_LIMITS_KEY,
            current.copy(
                image = image ?: current.image,
                video = video ?: current.video,
                audio = audio ?: current.audio,
            ),
        )
    }

    override fun getReviewPolicy(): ReviewPolicyRecord = getOrCreate(REVIEW_POLICY_KEY, DEFAULT_REVIEW_POLICY)

    override fun updateReviewPolicy(maxImageCount: Int, pointAmount: Int): ReviewPolicyRecord =
        save(REVIEW_POLICY_KEY, ReviewPolicyRecord(maxImageCount = maxImageCount, pointAmount = pointAmount))

    private inline fun <reified T> getOrCreate(settingKey: String, defaultValue: T): T =
        databaseFactory.withTransaction {
            val row = findRow(settingKey)
            if (row == null) {
                insertRow(settingKey, json.encodeToString(defaultValue))
                defaultValue
            } else {
                json.decodeFromString(row[SystemSettingsTable.settingValue])
            }
        }

    private inline fun <reified T> save(settingKey: String, value: T): T =
        databaseFactory.withTransaction {
            val payload = json.encodeToString(value)
            val row = findRow(settingKey)
            if (row == null) {
                insertRow(settingKey, payload)
            } else {
                SystemSettingsTable.update({ (SystemSettingsTable.settingGroup eq GROUP) and (SystemSettingsTable.settingKey eq settingKey) }) {
                    it[settingValue] = payload
                    it[updatedAt] = Instant.now()
                }
            }
            value
        }

    private fun org.jetbrains.exposed.sql.Transaction.findRow(settingKey: String) =
        SystemSettingsTable.selectAll()
            .where { (SystemSettingsTable.settingGroup eq GROUP) and (SystemSettingsTable.settingKey eq settingKey) }
            .singleOrNull()

    private fun org.jetbrains.exposed.sql.Transaction.insertRow(settingKey: String, payload: String) {
        val now = Instant.now()
        SystemSettingsTable.insertAndGetId {
            it[settingGroup] = GROUP
            it[SystemSettingsTable.settingKey] = settingKey
            it[settingValue] = payload
            it[createdAt] = now
            it[updatedAt] = now
        }
    }

    companion object {
        private const val GROUP = "admin_settings"
        private const val ALLOWED_EXTENSIONS_KEY = "allowed_extensions"
        private const val UPLOAD_LIMITS_KEY = "upload_limits"
        private const val REVIEW_POLICY_KEY = "review_policy"

        private val DEFAULT_EXTENSIONS = AllowedExtensionsRecord(listOf("gif", "jpeg", "jpg", "mp3", "mp4", "pdf", "png", "webp"))
        private val DEFAULT_UPLOAD_LIMITS = UploadLimitsRecord(image = 5, video = 100, audio = 20)
        private val DEFAULT_REVIEW_POLICY = ReviewPolicyRecord(maxImageCount = 10, pointAmount = 500)
    }
}
