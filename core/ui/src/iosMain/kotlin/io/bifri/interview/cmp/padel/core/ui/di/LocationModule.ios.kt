package io.bifri.interview.cmp.padel.core.ui.di

import io.bifri.interview.cmp.padel.core.ui.location.GeocoderService
import io.bifri.interview.cmp.padel.core.ui.location.LocationService
import org.koin.dsl.module

actual val LocationModule = module {
    single { LocationService() }
    single { GeocoderService() }
}
