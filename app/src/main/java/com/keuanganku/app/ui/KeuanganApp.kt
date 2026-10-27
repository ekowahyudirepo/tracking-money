package com.keuanganku.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.res.painterResource
import com.keuanganku.app.R
import com.keuanganku.app.TransactionViewModel
import com.keuanganku.app.data.Transaction

private val TabLabels = listOf("Beranda", "Statistik", "Pengaturan")

@Composable
fun KeuanganApp(vm: TransactionViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                TabLabels.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Home, contentDescription = null)
                                1 -> Icon(painterResource(R.drawable.ic_chart), contentDescription = null)
                                else -> Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        },
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
