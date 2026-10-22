package io.bifri.interview.cmp.padel.feature.match.ui.component

import kotlin.test.Test
import kotlin.test.assertEquals

class SetScoreCellTest {

    @Test
    fun formatSetScore_regularScore_returnsPlainText() {
        val result = formatSetScore("6")
        assertEquals("6", result.text)
    }

    @Test
    fun formatSetScore_tiebreakScore_formatsWithSuperscript() {
        val result = formatSetScore("6(5)")
        assertEquals("6(5)", result.text)
    }

    @Test
    fun formatSetScore_nullOrBlank_returnsEmptyText() {
        assertEquals("", formatSetScore(null).text)
        assertEquals("", formatSetScore("").text)
        assertEquals("", formatSetScore("   ").text)
    }
}
