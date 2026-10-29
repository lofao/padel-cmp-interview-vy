package io.bifri.interview.cmp.padel.feature.tournament.data.repository

import io.bifri.interview.cmp.padel.core.location.Coordinates
import kotlinx.coroutines.flow.Flow

interface UserLocationRepository {
    fun observeUserLocation(): Flow<Coordinates?>
}
