@file:OptIn(ExperimentalMaterial3Api::class)

package com.keuanganku.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keuanganku.app.TransactionViewModel
import com.keuanganku.app.data.Transaction
import com.keuanganku.app.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val expenseCategories =
    listOf("Makan", "Transport", "Belanja", "Tagihan", "Hiburan", "Kesehatan", "Lainnya")
private val incomeCategories = listOf("Gaji", "Bonus", "Usaha", "Lainnya")

fun formatRupiah(value: Long): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    nf.maximumFractionDigits = 0
    return nf.format(value)
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(millis))

private fun startOfMonthMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@Composable
fun KeuanganApp(viewModel: TransactionViewModel) {
    val all by viewModel.transactions.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val monthStart = remember { startOfMonthMillis() }
    val thisMonth = all.filter { it.dateMillis >= monthStart }
    val income = thisMonth.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = thisMonth.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    Scaffold(
        topBar = { TopAppBar(title = { Text("KeuanganKu") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah transaksi")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SummaryCard(income = income, expense = expense)
            Spacer(Modifier.height(16.dp))
            Text("Riwayat Transaksi", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (all.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi. Tekan + untuk menambah.")
                }
            } else {
                LazyColumn {
                    items(all, key = { it.id }) { item ->
                        TransactionRow(item, onDelete = { viewModel.delete(item) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddTransactionDialog(
            onDismiss = { showDialog = false },
            onSave = { type, amount, category, note ->
                viewModel.add(type, amount, category, note)
                showDialog = false
            }
        )
    }
}

@Composable
private fun SummaryCard(income: Long, expense: Long) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Saldo bulan ini", style = MaterialTheme.typography.labelLarge)
            Text(
                formatRupiah(income - expense),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pemasukan", style = MaterialTheme.typography.labelMedium)
                    Text(formatRupiah(income), color = IncomeColor, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pengeluaran", style = MaterialTheme.typography.labelMedium)
                    Text(formatRupiah(expense), color = ExpenseColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(item: Transaction, onDelete: () -> Unit) {
    val isIncome = item.type == TransactionType.INCOME
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.category, fontWeight = FontWeight.SemiBold)
            if (item.note.isNotBlank()) {
                Text(item.note, style = MaterialTheme.typography.bodyMedium)
            }
            Text(formatDate(item.dateMillis), style = MaterialTheme.typography.labelSmall)
        }
        Text(
            (if (isIncome) "+ " else "- ") + formatRupiah(item.amount),
            color = if (isIncome) IncomeColor else ExpenseColor,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Hapus transaksi")
        }
    }
}

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Long, category: String, note: String) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(expenseCategories.first()) }
    var note by remember { mutableStateOf("") }

    val categories = if (type == TransactionType.INCOME) incomeCategories else expenseCategories
    val amount = amountText.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Transaksi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = {
                            type = TransactionType.EXPENSE
                            category = expenseCategories.first()
                        },
                        label = { Text("Pengeluaran") }
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = {
                            type = TransactionType.INCOME
                            category = incomeCategories.first()
                        },
                        label = { Text("Pemasukan") }
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v -> amountText = v.filter { it.isDigit() }.take(12) },
                    label = { Text("Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { c ->
                        FilterChip(
                            selected = c == category,
                            onClick = { category = c },
                            label = { Text(c) }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (opsional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(type, amount, category, note.trim()) },
                enabled = amount > 0
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
