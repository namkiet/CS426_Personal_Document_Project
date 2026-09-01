package com.example.personaldocumentproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.personaldocumentproject.di.AppContainer
import com.example.personaldocumentproject.navigation.AppNavGraph
import com.example.personaldocumentproject.ui.theme.PersonalDocumentProjectTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalDocumentProjectTheme {
                AppNavGraph(container)
            }
        }
    }
}
