package io.bifri.interview.cmp.padel.core.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private data object TestKey1 : NavKey
private data object TestKey2 : NavKey

class NavBackStackExtTest {

    @Test
    fun pop_whenSizeGreaterThanOne_removesLastElementAndReturnsIt() {
        val backStack = NavBackStack<NavKey>().apply {
            add(TestKey1)
            add(TestKey2)
        }

        val popped = backStack.pop()

        assertEquals(TestKey2, popped)
        assertEquals(1, backStack.size)
        assertEquals(TestKey1, backStack.first())
    }

    @Test
    fun pop_whenSizeIsOne_doesNotRemoveElementAndReturnsNull() {
        val backStack = NavBackStack<NavKey>().apply {
            add(TestKey1)
        }

        val popped = backStack.pop()

        assertNull(popped)
        assertEquals(1, backStack.size)
        assertEquals(TestKey1, backStack.first())
    }

    @Test
    fun pop_whenEmpty_doesNotRemoveElementAndReturnsNull() {
        val backStack = NavBackStack<NavKey>()

        val popped = backStack.pop()

        assertNull(popped)
        assertEquals(0, backStack.size)
    }
}
