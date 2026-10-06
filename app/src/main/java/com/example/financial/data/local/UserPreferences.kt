package com.example.financial.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.financial.presentation.screen.settings.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CurrencyOption(
    val symbol: String,
    val code: String,
    val label: String,
    val isSuffix: Boolean = false
)

val availableCurrencies = listOf(
    CurrencyOption("$", "USD", "USD ($)"),
    CurrencyOption("€", "EUR", "EUR (€)"),
    CurrencyOption("£", "GBP", "GBP (£)"),
    CurrencyOption("₫", "VND", "VND (₫)", isSuffix = true),
    CurrencyOption("¥", "JPY", "JPY (¥)"),
    CurrencyOption("$", "CAD", "CAD ($)"),
    CurrencyOption("$", "AUD", "AUD ($)")
)

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _currency = MutableStateFlow(getSavedCurrency())
    val currency: StateFlow<CurrencyOption> = _currency.asStateFlow()

    private fun getSavedThemeMode(): AppThemeMode {
        val name = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try { AppThemeMode.valueOf(name) } catch (_: Exception) { AppThemeMode.SYSTEM }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    private fun getSavedCurrency(): CurrencyOption {
        val code = prefs.getString("currency_code", "USD") ?: "USD"
        return availableCurrencies.find { it.code == code } ?: availableCurrencies.first()
    }

    fun setCurrency(currency: CurrencyOption) {
        prefs.edit().putString("currency_code", currency.code).apply()
        _currency.value = currency
    }

    fun formatAmount(amount: Double): String {
        val selected = _currency.value
        val formattedNumber = String.format(java.util.Locale.getDefault(), "%.2f", amount)
        return if (selected.isSuffix) "$formattedNumber ${selected.symbol}" else "${selected.symbol}$formattedNumber"
    }
}
