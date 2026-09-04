package com.example.personaldocumentproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Manages the app's navigation backstack and provides methods for route transitions.
 */
class AppNavigator(initialRoute: Routes = Routes.Library) {
    val backStack: SnapshotStateList<Routes> = mutableStateListOf(initialRoute)

    val currentRoute: Routes
        get() = backStack.lastOrNull() ?: Routes.Library

    val canGoBack: Boolean
        get() = backStack.size > 1

    fun navigateTo(route: Routes) {
        backStack.add(route)
    }

    fun popBackStack(): Boolean {
        if (canGoBack) {
            backStack.removeAt(backStack.lastIndex)
            return true
        }
        return false
    }

    fun popToRoot() {
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun replace(route: Routes) {
        if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
        backStack.add(route)
    }
}

@Composable
fun rememberAppNavigator(initialRoute: Routes = Routes.Library): AppNavigator {
    return remember { AppNavigator(initialRoute) }
}
