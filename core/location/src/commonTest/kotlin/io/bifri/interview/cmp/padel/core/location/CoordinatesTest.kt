package io.bifri.interview.cmp.padel.core.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoordinatesTest {

    @Test
    fun distanceToKm_sameLocation_returnsZero() {
        val madrid = Coordinates(40.4168, -3.7038)
        val distance = madrid.distanceToKm(madrid)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun distanceToKm_madridToBarcelona_returnsApproximateDistance() {
        val madrid = Coordinates(40.4168, -3.7038)
        val barcelona = Coordinates(41.3851, 2.1734)
        val distance = madrid.distanceToKm(barcelona)
        assertTrue(distance in 500.0..520.0)
    }
}
