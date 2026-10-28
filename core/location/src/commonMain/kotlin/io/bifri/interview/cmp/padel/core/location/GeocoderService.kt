package io.bifri.interview.cmp.padel.core.location

expect class GeocoderService {
    suspend fun geocode(location: String?, country: String?): Coordinates?
}
