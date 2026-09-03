package com.example.personaldocumentproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaldocumentproject.di.AppContainer
import com.example.personaldocumentproject.navigation.AppNavGraph
import com.example.personaldocumentproject.ui.theme.PersonalDocumentProjectTheme
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DocumentApplication).container
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = container.mainViewModelFactory
            )

            val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()

            PersonalDocumentProjectTheme (
                darkTheme = isDarkMode
            ){
                AppNavGraph(container)
            }
        }
    }
}
