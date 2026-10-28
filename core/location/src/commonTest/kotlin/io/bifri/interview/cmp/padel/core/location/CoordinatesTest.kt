package io.bifri.interview.cmp.padel.core.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoordinatesTest {

    @Test
    fun distanceToKm_sameLocation_returnsZero() {
        val madrid = Coordinates(MadridLat, MadridLon)
        val distance = madrid.distanceToKm(madrid)
        assertEquals(0.0, distance, Delta)
    }

    @Test
    fun distanceToKm_madridToBarcelona_returnsApproximateDistance() {
        val madrid = Coordinates(MadridLat, MadridLon)
        val barcelona = Coordinates(BarcelonaLat, BarcelonaLon)
        val distance = madrid.distanceToKm(barcelona)
        assertTrue(distance in (MinDistanceKm..MaxDistanceKm))
    }

    companion object {
        private const val MadridLat = 40.4168
        private const val MadridLon = -3.7038
        private const val BarcelonaLat = 41.3851
        private const val BarcelonaLon = 2.1734
        private const val Delta = 0.001
        private const val MinDistanceKm = 500.0
        private const val MaxDistanceKm = 520.0
    }
}
