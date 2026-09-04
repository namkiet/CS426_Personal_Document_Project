package com.example.personaldocumentproject.navigation

import androidx.compose.runtime.saveable.listSaver

sealed interface Routes {
    data object Library : Routes
    data object Capture : Routes
    data class Review(val imageUri: String) : Routes
    data class Detail(val documentId: String) : Routes
    data object Setting : Routes

    companion object {
        val BackStackSaver = listSaver<List<Routes>, String>(
            save = { list ->
                list.map { route ->
                    when (route) {
                        Library -> "library"
                        Capture -> "capture"
                        is Review -> "review:${route.imageUri}"
                        is Detail -> "detail:${route.documentId}"
                        Setting -> "setting"
                    }
                }
            },
            restore = { list ->
                val restored = list.map { str ->
                    when {
                        str == "library" -> Library
                        str == "capture" -> Capture
                        str.startsWith("review:") -> Review(str.removePrefix("review:"))
                        str.startsWith("detail:") -> Detail(str.removePrefix("detail:"))
                        str == "setting" -> Setting
                        else -> Library
                    }
                }
                if (restored.isEmpty()) listOf(Library) else restored
            }
        )
    }
}
