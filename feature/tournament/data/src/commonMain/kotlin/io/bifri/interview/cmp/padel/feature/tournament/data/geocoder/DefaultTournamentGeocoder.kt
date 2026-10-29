package io.bifri.interview.cmp.padel.feature.tournament.data.geocoder

import io.bifri.interview.cmp.padel.core.location.CachedGeocoderService
import io.bifri.interview.cmp.padel.core.location.Coordinates
import io.bifri.interview.cmp.padel.feature.tournament.data.repository.TournamentGeocoder

class DefaultTournamentGeocoder(
    private val cachedGeocoderService: CachedGeocoderService,
) : TournamentGeocoder {
    override suspend fun geocodeBatch(
        locations: List<Pair<String?, String?>>,
    ): Map<String, Coordinates?> = cachedGeocoderService.geocodeBatch(locations)

    override fun buildLocationKey(
        location: String?,
        country: String?,
    ): String = cachedGeocoderService.buildLocationKey(location, country)
}
