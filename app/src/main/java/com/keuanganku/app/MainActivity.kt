package com.keuanganku.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.keuanganku.app.ui.KeuanganApp
import com.keuanganku.app.ui.KeuanganTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamic by viewModel.dynamicColor.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val dark = when (themeMode) {
                1 -> false
                2 -> true
                else -> systemDark
            }

            SideEffect {
                val transparent = android.graphics.Color.TRANSPARENT
                val style = if (dark) {
                    SystemBarStyle.dark(transparent)
                } else {
                    SystemBarStyle.light(transparent, transparent)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            KeuanganTheme(darkTheme = dark, dynamicColor = dynamic) {
                KeuanganApp(viewModel)
            }
        }
    }
}
