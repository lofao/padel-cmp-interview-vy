package io.bifri.interview.cmp.padel.feature.tournament.data.repository

import io.bifri.interview.cmp.padel.core.location.Coordinates
import io.bifri.interview.cmp.padel.core.location.LocationService
import kotlinx.coroutines.flow.Flow

class DefaultUserLocationRepository(
    private val locationService: LocationService,
) : UserLocationRepository {
    override fun observeUserLocation(): Flow<Coordinates?> = locationService.observeLocation()
}
