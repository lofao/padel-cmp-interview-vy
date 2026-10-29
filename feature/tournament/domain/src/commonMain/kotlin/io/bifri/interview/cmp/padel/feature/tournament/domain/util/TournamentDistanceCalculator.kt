package io.bifri.interview.cmp.padel.feature.tournament.domain.util

import io.bifri.interview.cmp.padel.feature.tournament.domain.model.LocationCoordinates
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournament
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournamentWithDistance

object TournamentDistanceCalculator {

    private const val MinimumDistanceChangeThresholdKm = 0.05 // 50 meters

    /**
     * Determines whether a GPS coordinate update represents a significant change (> 50 meters)
     * rather than minor GPS drift/noise.
     */
    fun isLocationChangeSignificant(old: LocationCoordinates?, new: LocationCoordinates?): Boolean = when {
        (old == null) && (new == null) -> false
        old == null -> true
        new == null -> true
        else -> old.distanceToKm(new) >= MinimumDistanceChangeThresholdKm
    }

    /**
     * Computes distance from [userLocation] to each tournament and sorts items by distance ascending.
     * Tournaments without valid geocoded coordinates are placed at the end.
     */
    fun sortByDistance(
        tournaments: List<TournamentsTournament>,
        userLocation: LocationCoordinates?,
        geocodedCoordinatesMap: Map<String, LocationCoordinates?>,
        locationKeyBuilder: (String?, String?) -> String,
    ): List<TournamentsTournamentWithDistance> {
        if (userLocation == null) {
            return tournaments.map { TournamentsTournamentWithDistance(tournament = it) }
        }

        return tournaments.asSequence().map { tournament ->
            val key = locationKeyBuilder(tournament.location, tournament.country)
            val coords = geocodedCoordinatesMap[key]
            val distanceKm = coords?.let { userLocation.distanceToKm(it) }
            TournamentsTournamentWithDistance(
                tournament = tournament,
                distanceKm = distanceKm,
            )
        }.sortedWith(compareBy { (_, distanceKm) -> distanceKm ?: Double.MAX_VALUE }).toList()
    }
}
