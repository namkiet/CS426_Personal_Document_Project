package com.example.personaldocumentproject.navigation

sealed interface Routes {
    data object Library : Routes
    data object Capture : Routes
    data class Review(val imageUri: String) : Routes
    data object Search : Routes
    data class Detail(val documentId: String) : Routes
}
