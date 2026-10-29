package io.bifri.interview.cmp.padel.feature.tournament.ui.geocoder

import io.bifri.interview.cmp.padel.core.location.CachedGeocoderService
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.LocationCoordinates
import io.bifri.interview.cmp.padel.feature.tournament.domain.repository.TournamentGeocoder

internal class DefaultTournamentGeocoder(
    private val cachedGeocoderService: CachedGeocoderService,
) : TournamentGeocoder {
    override suspend fun geocodeBatch(
        locations: List<Pair<String?, String?>>,
    ): Map<String, LocationCoordinates?> = cachedGeocoderService.geocodeBatch(locations).mapValues { (_, coords) ->
        coords?.let { LocationCoordinates(it.latitude, it.longitude) }
    }

    override fun buildLocationKey(
        location: String?,
        country: String?,
    ): String = cachedGeocoderService.buildLocationKey(location, country)
}
