package io.bifri.interview.cmp.padel.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLPlacemark
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class DefaultGeocoderService : GeocoderService {
    private val geocoder = CLGeocoder()

    override suspend fun geocode(location: String?, country: String?): Coordinates? {
        val query = listOfNotNull(location, country).filter { it.isNotBlank() }.joinToString(", ")
        if (query.isBlank()) return null

        val queryKey = query.lowercase()
        val locationKey = location?.lowercase().orEmpty()
        val fallback = FallbackCoordinates[queryKey] ?: FallbackCoordinates[locationKey]

        if (fallback != null) return fallback

        return suspendCancellableCoroutine { continuation ->
            geocoder.geocodeAddressString(query) { placemarks, _ ->
                val placemark = placemarks?.firstOrNull() as? CLPlacemark
                val clLocation = placemark?.location
                val coords = clLocation?.coordinate?.useContents {
                    Coordinates(latitude = latitude, longitude = longitude)
                }
                continuation.resume(coords ?: fallback)
            }
        }
    }
}
