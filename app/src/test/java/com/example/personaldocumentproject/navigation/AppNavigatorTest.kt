package com.example.personaldocumentproject.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {

    @Test
    fun initialRouteIsLibrary() {
        val navigator = AppNavigator()
        assertEquals(Routes.Library, navigator.currentRoute)
        assertEquals(1, navigator.backStack.size)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun navigateToPushesRoute() {
        val navigator = AppNavigator()
        navigator.navigateTo(Routes.Detail("doc-123"))

        assertEquals(Routes.Detail("doc-123"), navigator.currentRoute)
        assertEquals(2, navigator.backStack.size)
        assertTrue(navigator.canGoBack)
    }

    @Test
    fun popBackStackPopsTopRoute() {
        val navigator = AppNavigator()
        navigator.navigateTo(Routes.Setting)
        navigator.navigateTo(Routes.Detail("doc-456"))

        assertEquals(Routes.Detail("doc-456"), navigator.currentRoute)

        val poppedFirst = navigator.popBackStack()
        assertTrue(poppedFirst)
        assertEquals(Routes.Setting, navigator.currentRoute)

        val poppedSecond = navigator.popBackStack()
        assertTrue(poppedSecond)
        assertEquals(Routes.Library, navigator.currentRoute)

        val poppedRoot = navigator.popBackStack()
        assertFalse(poppedRoot)
        assertEquals(Routes.Library, navigator.currentRoute)
    }

    @Test
    fun popToRootClearsAllExceptInitial() {
        val navigator = AppNavigator()
        navigator.navigateTo(Routes.Setting)
        navigator.navigateTo(Routes.Detail("doc-1"))
        navigator.navigateTo(Routes.Review("uri://image"))

        assertEquals(4, navigator.backStack.size)
        navigator.popToRoot()

        assertEquals(1, navigator.backStack.size)
        assertEquals(Routes.Library, navigator.currentRoute)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun replaceChangesCurrentRoute() {
        val navigator = AppNavigator()
        navigator.navigateTo(Routes.Setting)
        navigator.replace(Routes.Detail("doc-999"))

        assertEquals(2, navigator.backStack.size)
        assertEquals(Routes.Detail("doc-999"), navigator.currentRoute)
    }
}
