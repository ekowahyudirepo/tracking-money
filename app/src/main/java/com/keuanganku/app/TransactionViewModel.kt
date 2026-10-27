package com.keuanganku.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.keuanganku.app.data.AppDatabase
import com.keuanganku.app.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).transactionDao()

    val transactions: StateFlow<List<Transaction>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(type: String, amount: Long, category: String, note: String) {
        viewModelScope.launch {
            dao.insert(
                Transaction(
                    type = type,
                    amount = amount,
                    category = category,
                    note = note,
                    dateMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { dao.delete(transaction) }
    }
}
