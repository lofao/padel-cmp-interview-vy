package io.bifri.interview.cmp.padel.feature.tournament.domain.util

import io.bifri.interview.cmp.padel.feature.tournament.domain.model.LocationCoordinates
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournament
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TournamentDistanceCalculatorTest {

    @Test
    fun isLocationChangeSignificant_detectsMinorAndMajorChanges() {
        val initial = LocationCoordinates(40.4168000, -3.7038000)
        val minorJitter = LocationCoordinates(40.4168010, -3.7038000) // ~1.1 meters
        val majorChange = LocationCoordinates(41.3851000, 2.1734000) // Barcelona (~500 km)

        assertFalse(TournamentDistanceCalculator.isLocationChangeSignificant(initial, minorJitter))
        assertTrue(TournamentDistanceCalculator.isLocationChangeSignificant(initial, majorChange))
        assertTrue(TournamentDistanceCalculator.isLocationChangeSignificant(null, initial))
        assertTrue(TournamentDistanceCalculator.isLocationChangeSignificant(initial, null))
        assertFalse(TournamentDistanceCalculator.isLocationChangeSignificant(null, null))
    }

    @Test
    fun sortByDistance_sortsTournamentsByProximityToUser() {
        val madridCoords = LocationCoordinates(40.4168, -3.7038)
        val barcelonaCoords = LocationCoordinates(41.3851, 2.1734)

        val tournaments = listOf(
            TournamentsTournament(
                id = 1,
                name = "Barcelona Open",
                location = "Barcelona",
                country = "ES",
                level = "P1",
                status = "Finished",
                startDate = null,
                endDate = null,
            ),
            TournamentsTournament(
                id = 2,
                name = "Madrid Open",
                location = "Madrid",
                country = "ES",
                level = "Master",
                status = "Finished",
                startDate = null,
                endDate = null,
            ),
        )

        val userInMadrid = LocationCoordinates(40.4168, -3.7038)
        val geocodedMap = mapOf(
            "barcelona|es" to barcelonaCoords,
            "madrid|es" to madridCoords,
        )

        val result = TournamentDistanceCalculator.sortByDistance(
            tournaments = tournaments,
            userLocation = userInMadrid,
            geocodedCoordinatesMap = geocodedMap,
        ) { loc, country -> "${loc?.lowercase()}|${country?.lowercase()}" }

        assertEquals("Madrid Open", result[0].tournament.name)
        assertEquals("Barcelona Open", result[1].tournament.name)
    }

    @Test
    fun sortByDistance_nullLocation_returnsUnsortedWithNullDistances() {
        val tournaments = listOf(
            TournamentsTournament(
                id = 1,
                name = "Barcelona Open",
                location = "Barcelona",
                country = "ES",
                level = "P1",
                status = "Finished",
                startDate = null,
                endDate = null,
            ),
        )

        val result = TournamentDistanceCalculator.sortByDistance(
            tournaments = tournaments,
            userLocation = null,
            geocodedCoordinatesMap = emptyMap(),
        ) { loc, country -> "${loc?.lowercase()}|${country?.lowercase()}" }

        assertEquals(1, result.size)
        assertEquals(null, result[0].distanceKm)
    }
}
