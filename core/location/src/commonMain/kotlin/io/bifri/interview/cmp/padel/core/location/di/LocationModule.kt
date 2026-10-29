package io.bifri.interview.cmp.padel.core.location.di

import io.bifri.interview.cmp.padel.core.location.CachedGeocoderService
import io.bifri.interview.cmp.padel.core.location.GeocoderService
import io.bifri.interview.cmp.padel.core.location.LocationService
import org.koin.core.scope.Scope
import org.koin.dsl.module

val LocationModule = module {
    single<LocationService> { createPlatformLocationService() }
    single<GeocoderService> { CachedGeocoderService(delegate = createPlatformGeocoderService()) }
    single { get<GeocoderService>() as CachedGeocoderService }
}

internal expect fun Scope.createPlatformLocationService(): LocationService
internal expect fun Scope.createPlatformGeocoderService(): GeocoderService
