package com.keuanganku.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keuanganku.app.TransactionViewModel
import com.keuanganku.app.data.TransactionType
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun StatsScreen(vm: TransactionViewModel, padding: PaddingValues) {
    val all by vm.transactions.collectAsState()
    val month by vm.month.collectAsState()

    val monthItems = remember(all, month) { all.filter { monthIndexOf(it.dateMillis) == month } }
    val expenses = monthItems.filter { it.type == TransactionType.EXPENSE }
    val totalIncome = monthItems.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }

    val byCategory = remember(monthItems) {
        monthItems.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .map { it.key to it.value.sumOf { t -> t.amount } }
            .sortedByDescending { it.second }
    }

    // Pemasukan & pengeluaran 6 bulan terakhir (sampai bulan yang dipilih)
    val series = remember(all, month) {
        val perMonth = all.groupBy { monthIndexOf(it.dateMillis) }
        ((month - 5)..month).map { idx ->
            val list = perMonth[idx].orEmpty()
            Triple(
                idx,
                list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }
    }

    val daysCounted =
        if (month == currentMonthIndex()) Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        else daysInMonth(month)
    val avgPerDay = if (daysCounted > 0) totalExpense / daysCounted else 0L
    val biggest = expenses.maxByOrNull { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            SectionCard("Ringkasan " + monthLabel(month)) {
                StatLine("Jumlah transaksi", monthItems.size.toString())
                StatLine("Rata-rata pengeluaran per hari", formatRupiah(avgPerDay))
                StatLine(
                    "Pengeluaran terbesar",
                    if (biggest != null) biggest.category + " • " + formatRupiah(biggest.amount) else "-"
                )
                StatLine(
                    "Rasio tabungan",
                    if (totalIncome > 0) {
                        (((totalIncome - totalExpense).toDouble() / totalIncome) * 100).roundToInt().toString() + "%"
                    } else "-"
                )
            }
        }

        item {
            SectionCard("6 bulan terakhir") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(incomeColor(), "Pemasukan")
                    LegendDot(expenseColor(), "Pengeluaran")
                }
                MonthlyChart(series = series, selected = month)
            }
        }

        item {
            SectionCard("Pengeluaran per kategori") {
                if (byCategory.isEmpty()) {
                    Text(
                        "Belum ada pengeluaran di bulan ini.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    byCategory.forEach { (name, sum) ->
                        val fraction = if (totalExpense > 0) sum.toFloat() / totalExpense.toFloat() else 0f
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name, fontWeight = FontWeight.SemiBold)
                                Text(formatRupiah(sum) + " (" + (fraction * 100).roundToInt() + "%)")
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = expenseColor(),
                                trackColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(12.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MonthlyChart(series: List<Triple<Int, Long, Long>>, selected: Int) {
    val maxValue = (series.maxOfOrNull { maxOf(it.second, it.third) } ?: 0L).coerceAtLeast(1L)
    Row(
        Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        series.forEach { (idx, inc, exp) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Bar(inc, maxValue, incomeColor())
                    Bar(exp, maxValue, expenseColor())
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    monthShort(idx),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (idx == selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun Bar(value: Long, max: Long, color: Color) {
    val heightDp = if (value <= 0L) 2f else (100f * value / max).coerceAtLeast(3f)
    Box(
        Modifier
            .width(12.dp)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            .background(color)
    )
}
