package io.bifri.interview.cmp.padel.core.location

import kotlinx.coroutines.flow.Flow

expect class LocationService {
    fun observeLocation(): Flow<Coordinates?>
}
