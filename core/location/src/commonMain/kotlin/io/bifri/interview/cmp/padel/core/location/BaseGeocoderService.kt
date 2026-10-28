package io.bifri.interview.cmp.padel.core.location

abstract class BaseGeocoderService : GeocoderService {
    override suspend fun geocode(location: String?, country: String?): Coordinates? {
        val query = listOfNotNull(location, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        if (query.isBlank()) return null

        val queryKey = query.lowercase()
        val locationKey = location?.lowercase().orEmpty()
        val fallback = FallbackCoordinates[queryKey] ?: FallbackCoordinates[locationKey]

        return fallback ?: geocodePlatform(query)
    }

    protected abstract suspend fun geocodePlatform(query: String): Coordinates?
}
