package io.bifri.interview.cmp.padel.core.location

import kotlinx.coroutines.flow.Flow

interface LocationService {
    fun observeLocation(): Flow<Coordinates?>
    fun onPermissionUpdated() {}
}
