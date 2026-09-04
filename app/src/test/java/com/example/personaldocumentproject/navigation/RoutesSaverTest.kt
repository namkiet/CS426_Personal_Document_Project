package com.example.personaldocumentproject.navigation

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesSaverTest {
    private val dummySaverScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    @Test
    fun backStackSaver_serializes_and_restores_all_routes() {
        val originalStack: List<Routes> = listOf(
            Routes.Library,
            Routes.Capture,
            Routes.Review("content://media/external/images/media/42?foo=bar#baz"),
            Routes.Detail("doc-1234"),
            Routes.ModelManager,
        )

        val saved = with(Routes.BackStackSaver) { dummySaverScope.save(originalStack) }
        val restored = Routes.BackStackSaver.restore(saved!!)

        assertEquals(originalStack, restored)
    }

    @Test
    fun backStackSaver_handles_empty_or_unknown_routes_gracefully() {
        val restoredEmpty = Routes.BackStackSaver.restore(emptyList<String>())
        assertEquals(listOf(Routes.Library), restoredEmpty)

        val restoredUnknown = Routes.BackStackSaver.restore(listOf("unknown_route"))
        assertEquals(listOf(Routes.Library), restoredUnknown)
    }
}
