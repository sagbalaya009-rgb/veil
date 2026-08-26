package com.veil.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "veil_settings")
data class VeilSettingsEntity(
    @androidx.room.PrimaryKey val id: Int = 1,
    val onboardingComplete: Boolean = false,
    val protectionLevel: String = "BALANCED",
    val cooldownMinutes: Long = 10,
    val recoveryTask: String = "BREATHING",
    val lockPeriod: String = "TWO_HOURS",
)

@Entity(tableName = "protection_events")
data class ProtectionEventEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val atEpochMillis: Long,
    val confidence: Float? = null,
)

@Dao
interface VeilSettingsDao {
    @Query("SELECT * FROM veil_settings WHERE id = 1")
    suspend fun get(): VeilSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: VeilSettingsEntity)
}

@Dao
interface ProtectionEventDao {
    @Query("SELECT * FROM protection_events ORDER BY atEpochMillis DESC")
    fun observeAll(): Flow<List<ProtectionEventEntity>>

    @Insert
    suspend fun insert(event: ProtectionEventEntity)

    @Query("SELECT COUNT(*) FROM protection_events")
    suspend fun count(): Int
}

@Database(
    entities = [VeilSettingsEntity::class, ProtectionEventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class VeilDatabase : RoomDatabase() {
    abstract fun settingsDao(): VeilSettingsDao
    abstract fun eventDao(): ProtectionEventDao
}
