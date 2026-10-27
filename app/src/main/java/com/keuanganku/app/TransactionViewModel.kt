package com.keuanganku.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.keuanganku.app.data.AppDatabase
import com.keuanganku.app.data.Transaction
import com.keuanganku.app.ui.currentMonthIndex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).transactionDao()
    private val prefs = app.getSharedPreferences("keuanganku_prefs", Context.MODE_PRIVATE)

    val transactions: StateFlow<List<Transaction>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bulan yang sedang ditampilkan (tahun * 12 + bulan 0..11)
    private val _month = MutableStateFlow(currentMonthIndex())
    val month: StateFlow<Int> = _month.asStateFlow()

    fun shiftMonth(delta: Int) {
        _month.value = _month.value + delta
    }

    fun goToCurrentMonth() {
        _month.value = currentMonthIndex()
    }

    // Anggaran pengeluaran bulanan (0 = nonaktif)
    private val _budget = MutableStateFlow(prefs.getLong("budget", 0L))
    val budget: StateFlow<Long> = _budget.asStateFlow()

    fun setBudget(value: Long) {
        prefs.edit().putLong("budget", value).apply()
        _budget.value = value
    }

    // Tema: 0 = ikut sistem, 1 = terang, 2 = gelap
    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 0))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean("dynamic_color", false))
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean("dynamic_color", enabled).apply()
        _dynamicColor.value = enabled
    }

    fun save(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.id == 0L) dao.insert(transaction) else dao.update(transaction)
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { dao.delete(transaction) }
    }

    fun deleteAll() {
        viewModelScope.launch { dao.deleteAll() }
    }
}
