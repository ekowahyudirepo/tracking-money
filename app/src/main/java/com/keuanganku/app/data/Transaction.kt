package com.keuanganku.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object TransactionType {
    const val INCOME = "INCOME"
    const val EXPENSE = "EXPENSE"
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Long,
    val category: String,
    val note: String,
    val dateMillis: Long
)
