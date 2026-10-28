package io.bifri.interview.cmp.padel.core.location

interface GeocoderService {
    suspend fun geocode(location: String?, country: String?): Coordinates?
}
