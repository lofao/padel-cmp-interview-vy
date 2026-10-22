package io.bifri.interview.cmp.padel.core.ui.location

expect class GeocoderService {
    suspend fun geocode(location: String?, country: String?): Coordinates?
}
