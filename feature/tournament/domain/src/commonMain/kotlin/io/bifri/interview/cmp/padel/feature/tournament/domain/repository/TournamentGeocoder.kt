package io.bifri.interview.cmp.padel.feature.tournament.domain.repository

import io.bifri.interview.cmp.padel.feature.tournament.domain.model.LocationCoordinates

interface TournamentGeocoder {
    suspend fun geocodeBatch(locations: List<Pair<String?, String?>>): Map<String, LocationCoordinates?>
    fun buildLocationKey(location: String?, country: String?): String
}
