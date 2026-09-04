package com.example.personaldocumentproject.presentation.library.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.personaldocumentproject.ui.imageVector.FluentuiSystemIconsPerson
import com.example.personaldocumentproject.ui.imageVector.FluentuiSystemIconsSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTopBar(
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(text = "Library")
        },
        actions = {
            IconButton(onClick = onSettingClick) {
                Icon(
                    imageVector = FluentuiSystemIconsSettings,
                    contentDescription = "setting"
                )
            }
        },
        modifier = modifier
    )
}
