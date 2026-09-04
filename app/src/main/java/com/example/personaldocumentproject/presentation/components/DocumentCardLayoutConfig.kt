package com.example.personaldocumentproject.presentation.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.domain.model.UserSetting

data class DocumentCardLayoutConfig (
    val showImage: Boolean,
    val cardWidth: Dp,
    val cardHeight: Dp,
)

fun UserSetting.toLayoutConfig() : DocumentCardLayoutConfig {
    return DocumentCardLayoutConfig(
        showImage = showImage,
        cardWidth = (60 + (widthValue * 120)).dp,
        cardHeight = (60 + (heightValue * 120)).dp,
    )
}