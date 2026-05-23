package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "trading_days")
data class TradingDay(
    @PrimaryKey val sn: Int,
    val currentLoss: Double = 0.0,
    val status: String? = null, // "complete", "fail", "loss", null
    val isManualAmount: Boolean = false,
    val manualAmount: Double = 0.0,
    val isManualProfit: Boolean = false,
    val manualProfit: Double = 0.0
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val totalDays: Int = 30,
    val startAmount: Double = 10.0,
    val profitIncrement: Double = 2.0
)

@Dao
interface TradingDao {
    @Query("SELECT * FROM trading_days ORDER BY sn ASC")
    fun getAllTradingDaysFlow(): Flow<List<TradingDay>>

    @Query("SELECT * FROM trading_days ORDER BY sn ASC")
    suspend fun getAllTradingDays(): List<TradingDay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTradingDay(day: TradingDay)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTradingDays(days: List<TradingDay>)

    @Query("DELETE FROM trading_days")
    suspend fun deleteAllTradingDays()

    @Query("DELETE FROM trading_days WHERE sn > :count")
    suspend fun deleteTradingDaysAbove(count: Int)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getAppSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(settings: AppSettings)
}

@Database(entities = [TradingDay::class, AppSettings::class], version = 2, exportSchema = false)
abstract class TradingDatabase : RoomDatabase() {
    abstract val tradingDao: TradingDao

    companion object {
        @Volatile
        private var INSTANCE: TradingDatabase? = null

        fun getDatabase(context: Context): TradingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TradingDatabase::class.java,
                    "trading_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
