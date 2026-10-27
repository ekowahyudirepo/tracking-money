package com.keuanganku.app.ui

import com.keuanganku.app.data.Transaction
import com.keuanganku.app.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val LocaleId: Locale = Locale("id", "ID")

data class Category(val name: String)

val ExpenseCategories = listOf(
    Category("Makan"),
    Category("Transport"),
    Category("Belanja"),
    Category("Tagihan"),
    Category("Hiburan"),
    Category("Kesehatan"),
    Category("Pendidikan"),
    Category("Keluarga"),
    Category("Lainnya")
)

val IncomeCategories = listOf(
    Category("Gaji"),
    Category("Bonus"),
    Category("Usaha"),
    Category("Investasi"),
    Category("Lainnya")
)

fun categoriesFor(type: String): List<Category> =
    if (type == TransactionType.INCOME) IncomeCategories else ExpenseCategories

/** Huruf pertama kategori, dipakai sebagai penanda polos di daftar transaksi. */
fun categoryInitial(name: String): String = name.take(1).uppercase()

// ---------- Uang ----------

fun formatRupiah(value: Long): String {
    val nf = NumberFormat.getCurrencyInstance(LocaleId)
    nf.maximumFractionDigits = 0
    return nf.format(value)
}

/** 1500000 -> "1,5 jt", 25000 -> "25 rb" */
fun formatCompact(value: Long): String {
    fun trim(x: Double): String =
        String.format(LocaleId, "%.1f", x).removeSuffix(",0").removeSuffix(".0")
    return when {
        value >= 1_000_000_000L -> trim(value / 1_000_000_000.0) + " M"
        value >= 1_000_000L -> trim(value / 1_000_000.0) + " jt"
        value >= 1_000L -> trim(value / 1_000.0) + " rb"
        else -> value.toString()
    }
}

/** "1500000" -> "1.500.000" (untuk kolom input nominal) */
fun formatThousands(digits: String): String {
    val n = digits.toLongOrNull() ?: return ""
    return NumberFormat.getInstance(LocaleId).format(n)
}

fun parseAmount(text: String): Long =
    text.filter { it.isDigit() }.take(12).toLongOrNull() ?: 0L

// ---------- Tanggal ----------

private val MonthNames = arrayOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)
private val MonthShort = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)

/** Indeks bulan = tahun * 12 + bulan (0..11) */
fun monthIndexOf(millis: Long): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)
}

fun currentMonthIndex(): Int = monthIndexOf(System.currentTimeMillis())

fun monthLabel(index: Int): String = MonthNames[index % 12] + " " + (index / 12)

fun monthShort(index: Int): String = MonthShort[index % 12]

fun daysInMonth(index: Int): Int {
    val c = Calendar.getInstance()
    c.clear()
    c.set(index / 12, index % 12, 1)
    return c.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun dayKey(millis: Long): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH)
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", LocaleId).format(Date(millis))

fun dayLabel(millis: Long): String {
    val now = System.currentTimeMillis()
    val key = dayKey(millis)
    val base = SimpleDateFormat("EEEE, dd MMM yyyy", LocaleId).format(Date(millis))
    return when (key) {
        dayKey(now) -> "Hari ini • $base"
        dayKey(now - 24L * 60 * 60 * 1000) -> "Kemarin • $base"
        else -> base
    }
}

/** Date picker Material3 bekerja dengan tengah malam UTC; ubah dari/ke waktu lokal. */
fun localToUtcMidnight(local: Long): Long {
    val l = Calendar.getInstance()
    l.timeInMillis = local
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    u.clear()
    u.set(l.get(Calendar.YEAR), l.get(Calendar.MONTH), l.get(Calendar.DAY_OF_MONTH))
    return u.timeInMillis
}

fun utcMidnightToLocalNoon(utc: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    u.timeInMillis = utc
    val l = Calendar.getInstance()
    l.clear()
    l.set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH), 12, 0, 0)
    return l.timeInMillis
}

// ---------- Ekspor ----------

private fun csvEscape(s: String): String = "\"" + s.replace("\"", "\"\"") + "\""

fun buildCsv(items: List<Transaction>): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sb = StringBuilder("﻿")
    sb.append("tanggal,jenis,kategori,nominal,catatan\n")
    items.sortedBy { it.dateMillis }.forEach {
        sb.append(df.format(Date(it.dateMillis))).append(',')
        sb.append(if (it.type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran").append(',')
        sb.append(csvEscape(it.category)).append(',')
        sb.append(it.amount).append(',')
        sb.append(csvEscape(it.note)).append('\n')
    }
    return sb.toString()
}

fun buildSummaryText(month: Int, items: List<Transaction>): String {
    val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val sb = StringBuilder()
    sb.append("Ringkasan keuangan ").append(monthLabel(month)).append('\n')
    sb.append("Pemasukan: ").append(formatRupiah(income)).append('\n')
    sb.append("Pengeluaran: ").append(formatRupiah(expense)).append('\n')
    sb.append("Saldo: ").append(formatRupiah(income - expense)).append('\n')
    val byCat = items.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .map { it.key to it.value.sumOf { t -> t.amount } }
        .sortedByDescending { it.second }
    if (byCat.isNotEmpty()) {
        sb.append("\nPengeluaran per kategori:\n")
        byCat.forEach { sb.append("- ").append(it.first).append(": ").append(formatRupiah(it.second)).append('\n') }
    }
    return sb.toString().trimEnd()
}
