package com.example.personaldocumentproject.presentation.review.components

import androidx.compose.runtime.Composable
import com.example.personaldocumentproject.ui.components.AppTextField

@Composable
fun DraftTitleField(value: String, onValueChange: (String) -> Unit) {
    AppTextField(value = value, onValueChange = onValueChange, placeholder = "Title", singleLine = true)
}
