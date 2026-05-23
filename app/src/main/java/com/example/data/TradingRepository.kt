package com.example.data

import kotlinx.coroutines.flow.Flow

class TradingRepository(private val tradingDao: TradingDao) {
    val allTradingDays: Flow<List<TradingDay>> = tradingDao.getAllTradingDaysFlow()
    val appSettings: Flow<AppSettings?> = tradingDao.getAppSettingsFlow()

    suspend fun getAppSettings(): AppSettings? = tradingDao.getAppSettings()

    suspend fun saveAppSettings(settings: AppSettings) {
        tradingDao.insertAppSettings(settings)
    }

    suspend fun getAllTradingDays(): List<TradingDay> = tradingDao.getAllTradingDays()

    suspend fun saveTradingDay(day: TradingDay) {
        tradingDao.insertTradingDay(day)
    }

    suspend fun saveTradingDays(days: List<TradingDay>) {
        tradingDao.insertTradingDays(days)
    }

    suspend fun deleteTradingDaysAbove(count: Int) {
        tradingDao.deleteTradingDaysAbove(count)
    }

    suspend fun deleteDays() {
        tradingDao.deleteAllTradingDays()
    }
}
