package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalculatedTradingDay(
    val sn: Int,
    val amount: Double,
    val profit: Double,
    val currentLoss: Double,
    val status: String?, // "complete", "fail", "loss", null
    val isManualAmount: Boolean,
    val manualAmount: Double,
    val isManualProfit: Boolean,
    val manualProfit: Double
)

data class QuotexUiState(
    val days: List<CalculatedTradingDay> = emptyList(),
    val startAmount: Double = 10.0,
    val profitIncrement: Double = 2.0,
    val totalDays: Int = 30,
    val totalCapital: Double = 10.0,
    val accumulatedProfit: Double = 0.0,
    val accumulatedLoss: Double = 0.0,
    val profitDaysCount: Int = 0,
    val lossDaysCount: Int = 0,
    val activeDaySn: Int = 1,
    val isLoading: Boolean = true,
    val themeMode: String = "dark",
    val isPinEnabled: Boolean = false,
    val pinCode: String = "",
    val isAppUnlocked: Boolean = true
)

class QuotexViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TradingDatabase.getDatabase(application)
    private val repository = TradingRepository(database.tradingDao)
    private val sharedPrefs = application.getSharedPreferences("quotex_settings", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(QuotexUiState())
    val uiState: StateFlow<QuotexUiState> = _uiState.asStateFlow()

    init {
        // Initialize from SharedPreferences
        val savedTheme = sharedPrefs.getString("theme_mode", "dark") ?: "dark"
        val savedIsPinEnabled = sharedPrefs.getBoolean("is_pin_enabled", false)
        val savedPinCode = sharedPrefs.getString("pin_code", "") ?: ""
        val initialUnlockState = !savedIsPinEnabled // start locked if pin is enabled
        
        _uiState.update {
            it.copy(
                themeMode = savedTheme,
                isPinEnabled = savedIsPinEnabled,
                pinCode = savedPinCode,
                isAppUnlocked = initialUnlockState
            )
        }

        viewModelScope.launch {
            // Check and initialize default settings and database content
            val defaultSettings = repository.getAppSettings()
            if (defaultSettings == null) {
                val initialSettings = AppSettings(
                    totalDays = 30,
                    startAmount = 10.0,
                    profitIncrement = 2.0
                )
                repository.saveAppSettings(initialSettings)
                val initialDays = (1..30).map { TradingDay(sn = it) }
                repository.saveTradingDays(initialDays)
            }
            
            // Combine calculations
            combine(
                repository.allTradingDays,
                repository.appSettings
            ) { days, settings ->
                if (settings == null) return@combine
                
                val currentSettings = settings
                val calculatedDays = calculateBalanceFlow(days, currentSettings)

                // Calculate cumulative stats
                var accumulatedProfit = 0.0
                var accumulatedLoss = 0.0
                var profitDaysCount = 0
                var lossDaysCount = 0

                calculatedDays.forEach { task ->
                    if (task.status == "complete") {
                        accumulatedProfit += (task.profit - task.amount)
                        profitDaysCount++
                    } else if (task.status == "loss") {
                        accumulatedLoss += task.currentLoss
                        lossDaysCount++
                    }
                }

                // Total Capital up to first pending day (or last day capitalization)
                var lastCapital = currentSettings.startAmount
                for (task in calculatedDays) {
                    when (task.status) {
                        "complete" -> lastCapital = task.profit
                        "loss" -> lastCapital = task.amount - task.currentLoss
                        "fail" -> lastCapital = task.amount
                        else -> break // Stop at first pending day
                    }
                }
                
                if (lastCapital < 0.0) lastCapital = 0.0

                val activeDaySn = calculatedDays.find { it.status == null }?.sn ?: -1

                _uiState.update {
                    it.copy(
                        days = calculatedDays,
                        startAmount = currentSettings.startAmount,
                        profitIncrement = currentSettings.profitIncrement,
                        totalDays = currentSettings.totalDays,
                        totalCapital = lastCapital,
                        accumulatedProfit = accumulatedProfit,
                        accumulatedLoss = accumulatedLoss,
                        profitDaysCount = profitDaysCount,
                        lossDaysCount = lossDaysCount,
                        activeDaySn = activeDaySn,
                        isLoading = false
                    )
                }
            }.flowOn(Dispatchers.Default).collect()
        }
    }

    private fun calculateBalanceFlow(
        dbDays: List<TradingDay>,
        settings: AppSettings
    ): List<CalculatedTradingDay> {
        val result = mutableListOf<CalculatedTradingDay>()
        var rollingBalance = settings.startAmount
        val profitInc = settings.profitIncrement

        for (i in 0 until settings.totalDays) {
            val dbDay = dbDays.find { it.sn == i + 1 } ?: TradingDay(sn = i + 1)

            val amount = if (dbDay.isManualAmount) {
                dbDay.manualAmount
            } else {
                rollingBalance
            }

            val profit = if (dbDay.isManualProfit) {
                dbDay.manualProfit
            } else {
                amount + profitInc
            }

            // Record rolling balance path
            when (dbDay.status) {
                "complete" -> {
                    rollingBalance = profit
                }
                "loss" -> {
                    rollingBalance = amount - dbDay.currentLoss
                    if (rollingBalance < 0.0) rollingBalance = 0.0
                }
                "fail" -> {
                    rollingBalance = amount
                }
                else -> {
                    rollingBalance = profit
                }
            }

            result.add(
                CalculatedTradingDay(
                    sn = i + 1,
                    amount = amount,
                    profit = profit,
                    currentLoss = dbDay.currentLoss,
                    status = dbDay.status,
                    isManualAmount = dbDay.isManualAmount,
                    manualAmount = dbDay.manualAmount,
                    isManualProfit = dbDay.isManualProfit,
                    manualProfit = dbDay.manualProfit
                )
            )
        }
        return result
    }

    fun updateStatus(sn: Int, status: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getAllTradingDays().find { it.sn == sn } ?: TradingDay(sn = sn)
            repository.saveTradingDay(existing.copy(status = status))
        }
    }

    fun logLoss(sn: Int, lossAmount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getAllTradingDays().find { it.sn == sn } ?: TradingDay(sn = sn)
            val updatedStatus = if (lossAmount > 0) "loss" else null
            repository.saveTradingDay(
                existing.copy(
                    currentLoss = lossAmount,
                    status = updatedStatus
                )
            )
        }
    }

    fun updateManualAmount(sn: Int, amount: Double, isManual: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getAllTradingDays().find { it.sn == sn } ?: TradingDay(sn = sn)
            repository.saveTradingDay(
                existing.copy(
                    isManualAmount = isManual,
                    manualAmount = amount
                )
            )
        }
    }

    fun resetInlineEditField(sn: Int, field: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getAllTradingDays().find { it.sn == sn } ?: TradingDay(sn = sn)
            when (field) {
                "amount" -> {
                    repository.saveTradingDay(existing.copy(isManualAmount = false))
                }
                "profit" -> {
                    repository.saveTradingDay(existing.copy(isManualProfit = false))
                }
                "loss" -> {
                    repository.saveTradingDay(existing.copy(currentLoss = 0.0, status = null))
                }
            }
        }
    }

    fun updateManualProfit(sn: Int, profit: Double, isManual: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getAllTradingDays().find { it.sn == sn } ?: TradingDay(sn = sn)
            repository.saveTradingDay(
                existing.copy(
                    isManualProfit = isManual,
                    manualProfit = profit
                )
            )
        }
    }

    fun updateSettings(totalDays: Int, startAmount: Double, profitIncrement: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            // Save settings
            val settings = AppSettings(
                totalDays = totalDays,
                startAmount = startAmount,
                profitIncrement = profitIncrement
            )
            repository.saveAppSettings(settings)

            // Adjust days list
            val existingDays = repository.getAllTradingDays()
            val maxCurrentSn = existingDays.maxOfOrNull { it.sn } ?: 0
            if (totalDays > maxCurrentSn) {
                val newDaysList = (maxCurrentSn + 1..totalDays).map { TradingDay(sn = it) }
                repository.saveTradingDays(newDaysList)
            } else if (totalDays < maxCurrentSn) {
                repository.deleteTradingDaysAbove(totalDays)
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDays()
            val defaultSettings = AppSettings(
                totalDays = 30,
                startAmount = 10.0,
                profitIncrement = 2.0
            )
            repository.saveAppSettings(defaultSettings)
            val defaultDays = (1..30).map { TradingDay(sn = it) }
            repository.saveTradingDays(defaultDays)
        }
    }

    fun exportDataToString(): String {
        return try {
            val root = JSONObject()
            val dataArray = JSONArray()
            _uiState.value.days.forEach { day ->
                val dayObj = JSONObject().apply {
                    put("sn", day.sn)
                    put("currentLoss", day.currentLoss)
                    put("status", day.status ?: JSONObject.NULL)
                    put("isManualAmount", day.isManualAmount)
                    put("manualAmount", day.manualAmount)
                    put("isManualProfit", day.isManualProfit)
                    put("manualProfit", day.manualProfit)
                }
                dataArray.put(dayObj)
            }
            root.put("data", dataArray)
            root.put("settings", JSONObject().apply {
                put("count", _uiState.value.totalDays)
                put("startAmount", _uiState.value.startAmount)
                put("profitInc", _uiState.value.profitIncrement)
            })
            root.put("exportDate", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()))
            root.toString(4)
        } catch (e: Exception) {
            ""
        }
    }

    fun importDataFromString(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val settingsObj = root.optJSONObject("settings")
            var importedDaysCount = 30
            var importedStartAmount = 10.0
            var importedProfitInc = 2.0

            if (settingsObj != null) {
                importedDaysCount = settingsObj.optInt("count", 30)
                importedStartAmount = settingsObj.optDouble("startAmount", 10.0)
                importedProfitInc = settingsObj.optDouble("profitInc", 2.0)
            }

            // Clean & sanitize imported configs
            val finalDaysCount = importedDaysCount.coerceIn(1, 365)
            val finalStartAmount = if (importedStartAmount.isNaN() || importedStartAmount.isInfinite() || importedStartAmount < 0.01) 10.0 else importedStartAmount
            val finalProfitInc = if (importedProfitInc.isNaN() || importedProfitInc.isInfinite() || importedProfitInc < 0.01) 2.0 else importedProfitInc

            val dataArray = root.optJSONArray("data")
            val daysList = mutableListOf<TradingDay>()
            if (dataArray != null) {
                for (i in 0 until dataArray.length()) {
                    val dayObj = dataArray.getJSONObject(i)
                    val sn = dayObj.optInt("sn", i + 1)
                    if (sn !in 1..finalDaysCount) continue // skip out-of-range days
                    
                    val status = if (dayObj.isNull("status")) null else dayObj.optString("status")
                    
                    val parsedLoss = dayObj.optDouble("currentLoss", 0.0)
                    val currentLoss = if (parsedLoss.isNaN() || parsedLoss.isInfinite() || parsedLoss < 0.0) 0.0 else parsedLoss
                    
                    val isManualAmount = dayObj.optBoolean("isManualAmount", false)
                    val parsedManualAmount = dayObj.optDouble("manualAmount", 0.0)
                    val manualAmount = if (parsedManualAmount.isNaN() || parsedManualAmount.isInfinite() || parsedManualAmount < 0.0) 0.0 else parsedManualAmount
                    
                    val isManualProfit = dayObj.optBoolean("isManualProfit", false)
                    val parsedManualProfit = dayObj.optDouble("manualProfit", 0.0)
                    val manualProfit = if (parsedManualProfit.isNaN() || parsedManualProfit.isInfinite() || parsedManualProfit < 0.0) 0.0 else parsedManualProfit
                    
                    daysList.add(
                        TradingDay(
                            sn = sn,
                            currentLoss = currentLoss,
                            status = status,
                            isManualAmount = isManualAmount,
                            manualAmount = manualAmount,
                            isManualProfit = isManualProfit,
                            manualProfit = manualProfit
                        )
                    )
                }
            }

            viewModelScope.launch(Dispatchers.IO) {
                repository.deleteDays()
                repository.saveAppSettings(
                    AppSettings(
                        totalDays = finalDaysCount,
                        startAmount = finalStartAmount,
                        profitIncrement = finalProfitInc
                    )
                )
                if (daysList.isNotEmpty()) {
                    // Filter duplicate serial numbers
                    val uniqueDays = daysList.associateBy { it.sn }.values.toList()
                    repository.saveTradingDays(uniqueDays)
                } else {
                    val defaultDays = (1..finalDaysCount).map { TradingDay(sn = it) }
                    repository.saveTradingDays(defaultDays)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateThemeMode(mode: String) {
        sharedPrefs.edit().putString("theme_mode", mode).apply()
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun enablePin(pin: String) {
        sharedPrefs.edit()
            .putBoolean("is_pin_enabled", true)
            .putString("pin_code", pin)
            .apply()
        _uiState.update {
            it.copy(
                isPinEnabled = true,
                pinCode = pin,
                isAppUnlocked = true // unlocked immediately on active setup
            )
        }
    }

    fun disablePin() {
        sharedPrefs.edit()
            .putBoolean("is_pin_enabled", false)
            .putString("pin_code", "")
            .apply()
        _uiState.update {
            it.copy(
                isPinEnabled = false,
                pinCode = "",
                isAppUnlocked = true
            )
        }
    }

    fun verifyPinAndUnlock(enteredPin: String): Boolean {
        val currentPin = _uiState.value.pinCode
        if (enteredPin == currentPin) {
            _uiState.update { it.copy(isAppUnlocked = true) }
            return true
        }
        return false
    }

    fun relockApp() {
        if (_uiState.value.isPinEnabled) {
            _uiState.update { it.copy(isAppUnlocked = false) }
        }
    }
}
