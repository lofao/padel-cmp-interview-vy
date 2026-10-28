package io.bifri.interview.cmp.padel.core.location.di

import io.bifri.interview.cmp.padel.core.location.DefaultGeocoderService
import io.bifri.interview.cmp.padel.core.location.DefaultLocationService
import io.bifri.interview.cmp.padel.core.location.GeocoderService
import io.bifri.interview.cmp.padel.core.location.LocationService
import org.koin.dsl.module

actual val LocationModule = module {
    single<LocationService> { DefaultLocationService() }
    single<GeocoderService> { DefaultGeocoderService() }
}
