package com.keuanganku.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keuanganku.app.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(vm: TransactionViewModel, padding: PaddingValues) {
    val context = LocalContext.current
    val all by vm.transactions.collectAsState()
    val month by vm.month.collectAsState()
    val budget by vm.budget.collectAsState()
    val themeMode by vm.themeMode.collectAsState()
    val dynamic by vm.dynamicColor.collectAsState()

    var budgetText by remember(budget) {
        mutableStateOf(if (budget > 0) formatThousands(budget.toString()) else "")
    }
    var showDeleteAll by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            val ok = try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(buildCsv(all).toByteArray(Charsets.UTF_8))
                }
                true
            } catch (e: Exception) {
                false
            }
            Toast.makeText(
                context,
                if (ok) "Data berhasil diekspor" else "Gagal mengekspor data",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pengaturan", style = MaterialTheme.typography.headlineSmall)

        SectionCard("Anggaran bulanan") {
            Text(
                "Batas pengeluaran per bulan. Kosongkan atau isi 0 untuk menonaktifkan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = budgetText,
                onValueChange = { v -> budgetText = formatThousands(v.filter { it.isDigit() }.take(12)) },
                label = { Text("Anggaran") },
                prefix = { Text("Rp ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                vm.setBudget(parseAmount(budgetText))
                Toast.makeText(context, "Anggaran disimpan", Toast.LENGTH_SHORT).show()
            }) { Text("Simpan anggaran") }
        }

        SectionCard("Tampilan") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = themeMode == 0, onClick = { vm.setThemeMode(0) }, label = { Text("Ikut sistem") })
                FilterChip(selected = themeMode == 1, onClick = { vm.setThemeMode(1) }, label = { Text("Terang") })
                FilterChip(selected = themeMode == 2, onClick = { vm.setThemeMode(2) }, label = { Text("Gelap") })
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Warna dinamis")
                        Text(
                            "Ikuti warna wallpaper (Android 12+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = dynamic, onCheckedChange = { vm.setDynamicColor(it) })
                }
            }
        }

        SectionCard("Data") {
            Text(
                "Total " + all.size + " transaksi tersimpan di perangkat ini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    val name = "keuanganku-" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) + ".csv"
                    exportLauncher.launch(name)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ekspor semua data ke CSV") }
            OutlinedButton(
                onClick = {
                    val monthItems = all.filter { monthIndexOf(it.dateMillis) == month }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, buildSummaryText(month, monthItems))
                    }
                    context.startActivity(Intent.createChooser(send, "Bagikan ringkasan"))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Bagikan ringkasan " + monthLabel(month)) }
            OutlinedButton(
                onClick = { showDeleteAll = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Hapus semua data") }
        }

        SectionCard("Tentang") {
            Text("KeuanganKu v1.1")
            Text(
                "Data tersimpan hanya di perangkat ini. Ekspor ke CSV secara berkala untuk cadangan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDeleteAll) {
        AlertDialog(
            onDismissRequest = { showDeleteAll = false },
            title = { Text("Hapus semua data?") },
            text = { Text("Seluruh transaksi akan dihapus permanen dan tidak bisa dikembalikan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteAll()
                        showDeleteAll = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus semua") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAll = false }) { Text("Batal") }
            }
        )
    }
}
