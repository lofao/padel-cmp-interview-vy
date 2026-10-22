package io.bifri.interview.cmp.padel.core.ui.location

import kotlinx.coroutines.flow.Flow

expect class LocationService {
    fun observeLocation(): Flow<Coordinates?>
}
