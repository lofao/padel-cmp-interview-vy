package io.bifri.interview.cmp.padel.core.location.di

import io.bifri.interview.cmp.padel.core.location.GeocoderService
import io.bifri.interview.cmp.padel.core.location.LocationService
import org.koin.dsl.module

actual val LocationModule = module {
    single { LocationService() }
    single { GeocoderService() }
}
