package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.model.ActionControlItem
import com.example.model.FollowUpExchange
import com.example.model.RiskLevel
import com.example.model.SstAuditReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "sst_audits")
data class SstAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val title: String,
    val sectorTag: String,
    val imageSourceLabel: String,
    val sampleDrawableRes: Int?,
    val customImageUri: String?,
    val riskLevelName: String,
    val severityLabel: String,
    val probabilityLabel: String,
    val hazardCategoriesJson: String,
    val section1Diagnostico: String,
    val section2Peligros: String,
    val section3Evaluacion: String,
    val section4Medidas: String,
    val section5Recomendaciones: String,
    val thoughtSummary: String?,
    val actionItemsJson: String,
    val followUpHistoryJson: String,
    val rawFullResponse: String
)

@Dao
interface SstAuditDao {
    @Query("SELECT * FROM sst_audits ORDER BY timestamp DESC")
    fun getAllAuditsFlow(): Flow<List<SstAuditEntity>>

    @Query("SELECT COUNT(*) FROM sst_audits")
    suspend fun getAuditsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(entity: SstAuditEntity): Long

    @Update
    suspend fun updateAudit(entity: SstAuditEntity)

    @Query("DELETE FROM sst_audits WHERE id = :id")
    suspend fun deleteAuditById(id: Long)
}

@Database(entities = [SstAuditEntity::class], version = 1, exportSchema = false)
abstract class SstAuditDatabase : RoomDatabase() {
    abstract fun sstAuditDao(): SstAuditDao

    companion object {
        @Volatile
        private var INSTANCE: SstAuditDatabase? = null

        fun getInstance(context: Context): SstAuditDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SstAuditDatabase::class.java,
                    "sst_audits_database.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class SstAuditRepository(private val dao: SstAuditDao) {
    private val json = Json { ignoreUnknownKeys = true }

    val allAudits: Flow<List<SstAuditReport>> = dao.getAllAuditsFlow().map { list ->
        list.map { it.toDomainModel(json) }
    }

    suspend fun getAuditsCount(): Int = dao.getAuditsCount()

    suspend fun saveAudit(report: SstAuditReport): Long {
        return dao.insertAudit(report.toEntity(json))
    }

    suspend fun updateAudit(report: SstAuditReport) {
        dao.updateAudit(report.toEntity(json))
    }

    suspend fun deleteAudit(id: Long) {
        dao.deleteAuditById(id)
    }

    private fun SstAuditEntity.toDomainModel(jsonCodec: Json): SstAuditReport {
        val categories = runCatching {
            jsonCodec.decodeFromString<List<String>>(hazardCategoriesJson)
        }.getOrDefault(emptyList())

        val actions = runCatching {
            jsonCodec.decodeFromString<List<ActionControlItem>>(actionItemsJson)
        }.getOrDefault(emptyList())

        val followUps = runCatching {
            jsonCodec.decodeFromString<List<FollowUpExchange>>(followUpHistoryJson)
        }.getOrDefault(emptyList())

        val parsedRisk = runCatching {
            RiskLevel.valueOf(riskLevelName)
        }.getOrDefault(RiskLevel.ALTO)

        return SstAuditReport(
            id = id,
            timestamp = timestamp,
            title = title,
            sectorTag = sectorTag,
            imageSourceLabel = imageSourceLabel,
            sampleDrawableRes = sampleDrawableRes,
            customImageUri = customImageUri,
            riskLevel = parsedRisk,
            severityLabel = severityLabel,
            probabilityLabel = probabilityLabel,
            hazardCategories = categories,
            section1Diagnostico = section1Diagnostico,
            section2Peligros = section2Peligros,
            section3Evaluacion = section3Evaluacion,
            section4Medidas = section4Medidas,
            section5Recomendaciones = section5Recomendaciones,
            thoughtSummary = thoughtSummary,
            actionItems = actions,
            followUpHistory = followUps,
            rawFullResponse = rawFullResponse
        )
    }

    private fun SstAuditReport.toEntity(jsonCodec: Json): SstAuditEntity {
        return SstAuditEntity(
            id = id,
            timestamp = timestamp,
            title = title,
            sectorTag = sectorTag,
            imageSourceLabel = imageSourceLabel,
            sampleDrawableRes = sampleDrawableRes,
            customImageUri = customImageUri,
            riskLevelName = riskLevel.name,
            severityLabel = severityLabel,
            probabilityLabel = probabilityLabel,
            hazardCategoriesJson = jsonCodec.encodeToString(hazardCategories),
            section1Diagnostico = section1Diagnostico,
            section2Peligros = section2Peligros,
            section3Evaluacion = section3Evaluacion,
            section4Medidas = section4Medidas,
            section5Recomendaciones = section5Recomendaciones,
            thoughtSummary = thoughtSummary,
            actionItemsJson = jsonCodec.encodeToString(actionItems),
            followUpHistoryJson = jsonCodec.encodeToString(followUpHistory),
            rawFullResponse = rawFullResponse
        )
    }
}
