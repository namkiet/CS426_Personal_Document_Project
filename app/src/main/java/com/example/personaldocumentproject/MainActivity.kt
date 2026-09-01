package com.example.personaldocumentproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.personaldocumentproject.di.AppContainer
import com.example.personaldocumentproject.navigation.AppNavGraph
import com.example.personaldocumentproject.ui.theme.PersonalDocumentProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DocumentApplication).container
        setContent {
            PersonalDocumentProjectTheme {
                AppNavGraph(container)
            }
        }
    }
}
