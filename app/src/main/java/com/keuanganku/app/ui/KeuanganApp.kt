package com.keuanganku.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.keuanganku.app.TransactionViewModel
import com.keuanganku.app.data.Transaction

private val Tabs = listOf("🏠" to "Beranda", "📊" to "Statistik", "⚙️" to "Pengaturan")

@Composable
fun KeuanganApp(vm: TransactionViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tabs.forEachIndexed { index, (emoji, label) ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Text(emoji) },
                        label = { Text(label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(onClick = { showAdd = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah transaksi")
                }
            }
        }
    ) { padding ->
        when (tab) {
            0 -> HomeScreen(vm, padding, onEdit = { editing = it })
            1 -> StatsScreen(vm, padding)
            else -> SettingsScreen(vm, padding)
        }
    }

    if (showAdd) {
        TransactionDialog(
            initial = null,
            onDismiss = { showAdd = false },
            onSave = {
                vm.save(it)
                showAdd = false
            }
        )
    }

    editing?.let { current ->
        TransactionDialog(
            initial = current,
            onDismiss = { editing = null },
            onSave = {
                vm.save(it)
                editing = null
            },
            onDelete = {
                vm.delete(it)
                editing = null
            }
        )
    }
}
