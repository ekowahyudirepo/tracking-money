@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.keuanganku.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keuanganku.app.data.Transaction
import com.keuanganku.app.data.TransactionType

@Composable
fun TransactionDialog(
    initial: Transaction?,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: ((Transaction) -> Unit)? = null
) {
    var type by remember { mutableStateOf(initial?.type ?: TransactionType.EXPENSE) }
    var amountText by remember {
        mutableStateOf(if (initial != null) formatThousands(initial.amount.toString()) else "")
    }
    var category by remember { mutableStateOf(initial?.category ?: ExpenseCategories.first().name) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var dateMillis by remember { mutableLongStateOf(initial?.dateMillis ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val amount = parseAmount(amountText)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Tambah Transaksi" else "Ubah Transaksi") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = {
                            if (type != TransactionType.EXPENSE) {
                                type = TransactionType.EXPENSE
                                category = ExpenseCategories.first().name
                            }
                        },
                        label = { Text("Pengeluaran") }
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = {
                            if (type != TransactionType.INCOME) {
                                type = TransactionType.INCOME
                                category = IncomeCategories.first().name
                            }
                        },
                        label = { Text("Pemasukan") }
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v ->
                        amountText = formatThousands(v.filter { it.isDigit() }.take(12))
                    },
                    label = { Text("Nominal") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Kategori", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categoriesFor(type).forEach { c ->
                        FilterChip(
                            selected = c.name == category,
                            onClick = { category = c.name },
                            label = { Text(c.name) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tanggal: " + formatDate(dateMillis))
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        Transaction(
                            id = initial?.id ?: 0L,
                            type = type,
                            amount = amount,
                            category = category,
                            note = note.trim(),
                            dateMillis = dateMillis
                        )
                    )
                },
                enabled = amount > 0
            ) { Text("Simpan") }
        },
        dismissButton = {
            Row {
                if (initial != null && onDelete != null) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Hapus") }
                }
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        }
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = localToUtcMidnight(dateMillis))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dateMillis = utcMidnightToLocalNoon(it) }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showDeleteConfirm && initial != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus transaksi?") },
            text = { Text(initial.category + " • " + formatRupiah(initial.amount)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(initial)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
            }
        )
    }
}
