package com.keuanganku.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.keuanganku.app.ui.KeuanganApp
import com.keuanganku.app.ui.KeuanganTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KeuanganTheme {
                KeuanganApp(viewModel)
            }
        }
    }
}
