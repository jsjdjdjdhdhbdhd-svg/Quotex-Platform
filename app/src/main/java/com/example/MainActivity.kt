package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.QuotexViewModel
import com.example.ui.screens.MainDashboard
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: QuotexViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()

      val isDark = when (uiState.themeMode) {
          "light" -> false
          "dark" -> true
          else -> androidx.compose.foundation.isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = isDark) {
         MainDashboard(
             uiState = uiState,
             viewModel = viewModel,
             modifier = Modifier.fillMaxSize()
         )
      }
    }
  }
}
