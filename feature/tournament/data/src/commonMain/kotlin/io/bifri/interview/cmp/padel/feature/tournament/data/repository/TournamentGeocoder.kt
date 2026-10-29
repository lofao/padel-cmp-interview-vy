package io.bifri.interview.cmp.padel.feature.tournament.data.repository

import io.bifri.interview.cmp.padel.core.location.Coordinates

interface TournamentGeocoder {
    suspend fun geocodeBatch(locations: List<Pair<String?, String?>>): Map<String, Coordinates?>
    fun buildLocationKey(location: String?, country: String?): String
}
