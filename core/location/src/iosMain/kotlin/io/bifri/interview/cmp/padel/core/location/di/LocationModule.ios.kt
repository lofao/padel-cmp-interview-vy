package io.bifri.interview.cmp.padel.core.location.di

import io.bifri.interview.cmp.padel.core.location.DefaultGeocoderService
import io.bifri.interview.cmp.padel.core.location.DefaultLocationService
import io.bifri.interview.cmp.padel.core.location.GeocoderService
import io.bifri.interview.cmp.padel.core.location.LocationService
import org.koin.core.scope.Scope

internal actual fun Scope.createPlatformLocationService(): LocationService =
    DefaultLocationService()

internal actual fun Scope.createPlatformGeocoderService(): GeocoderService =
    DefaultGeocoderService()
