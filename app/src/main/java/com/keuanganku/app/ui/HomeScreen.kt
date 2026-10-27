package com.keuanganku.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keuanganku.app.TransactionViewModel
import com.keuanganku.app.data.Transaction
import com.keuanganku.app.data.TransactionType

@Composable
fun HomeScreen(
    vm: TransactionViewModel,
    padding: PaddingValues,
    onEdit: (Transaction) -> Unit
) {
    val all by vm.transactions.collectAsState()
    val month by vm.month.collectAsState()
    val budget by vm.budget.collectAsState()
    var filter by rememberSaveable { mutableIntStateOf(0) } // 0 semua, 1 pemasukan, 2 pengeluaran
    var query by rememberSaveable { mutableStateOf("") }

    val monthItems = remember(all, month) { all.filter { monthIndexOf(it.dateMillis) == month } }
    val income = monthItems.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = monthItems.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val shown = remember(monthItems, filter, query) {
        monthItems.filter { t ->
            val typeOk = when (filter) {
                1 -> t.type == TransactionType.INCOME
                2 -> t.type == TransactionType.EXPENSE
                else -> true
            }
            val q = query.trim()
            val queryOk = q.isEmpty() ||
                t.category.contains(q, ignoreCase = true) ||
                t.note.contains(q, ignoreCase = true)
            typeOk && queryOk
        }
    }
    val grouped = remember(shown) { shown.groupBy { dayKey(it.dateMillis) }.entries.toList() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
    ) {
        item {
            MonthSelector(
                month = month,
                onPrev = { vm.shiftMonth(-1) },
                onNext = { vm.shiftMonth(1) },
                onToday = { vm.goToCurrentMonth() }
            )
        }
        item {
            Column {
                SummaryCard(month = month, income = income, expense = expense, budget = budget)
                Spacer(Modifier.height(16.dp))
            }
        }
        item {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Cari kategori atau catatan") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filter == 0, onClick = { filter = 0 }, label = { Text("Semua") })
                    FilterChip(selected = filter == 1, onClick = { filter = 1 }, label = { Text("Pemasukan") })
                    FilterChip(selected = filter == 2, onClick = { filter = 2 }, label = { Text("Pengeluaran") })
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (shown.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (monthItems.isEmpty()) "Belum ada transaksi di bulan ini.\nTekan + untuk menambah."
                        else "Tidak ada transaksi yang cocok.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        grouped.forEach { entry ->
            item(key = "day-" + entry.key) {
                DayHeader(entry.value)
            }
            items(entry.value, key = { it.id }) { t ->
                TransactionRow(t, onClick = { onEdit(t) })
            }
        }
    }
}

@Composable
private fun SummaryCard(month: Int, income: Long, expense: Long, budget: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Saldo " + monthLabel(month), style = MaterialTheme.typography.labelLarge)
                Text(
                    formatRupiah(income - expense),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                if (budget > 0) {
                    Spacer(Modifier.height(12.dp))
                    val ratio = expense.toFloat() / budget.toFloat()
                    val barColor = when {
                        ratio >= 1f -> MaterialTheme.colorScheme.error
                        ratio >= 0.8f -> Color(0xFFF9A825)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Anggaran pengeluaran", style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatCompact(expense) + " / " + formatCompact(budget),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { ratio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (expense <= budget) "Sisa " + formatRupiah(budget - expense)
                        else "Melebihi anggaran " + formatRupiah(expense - budget),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Pemasukan", style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatRupiah(income),
                        color = incomeColor(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Card(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Pengeluaran", style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatRupiah(expense),
                        color = expenseColor(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DayHeader(items: List<Transaction>) {
    val net = items.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            dayLabel(items.first().dateMillis),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            (if (net >= 0) "+" else "-") + formatCompact(kotlin.math.abs(net)),
            style = MaterialTheme.typography.labelMedium,
            color = if (net >= 0) incomeColor() else expenseColor()
        )
    }
}

@Composable
private fun TransactionRow(item: Transaction, onClick: () -> Unit) {
    val isIncome = item.type == TransactionType.INCOME
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(emojiFor(item.type, item.category))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.category, fontWeight = FontWeight.SemiBold)
            if (item.note.isNotBlank()) {
                Text(
                    item.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            (if (isIncome) "+" else "-") + formatRupiah(item.amount),
            color = if (isIncome) incomeColor() else expenseColor(),
            fontWeight = FontWeight.SemiBold
        )
    }
}
