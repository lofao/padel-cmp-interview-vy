package io.bifri.interview.cmp.padel.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLPlacemark
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class DefaultGeocoderService : BaseGeocoderService() {
    override suspend fun geocodePlatform(query: String): Coordinates? {
        val geocoder = CLGeocoder()
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { geocoder.cancelGeocode() }

            geocoder.geocodeAddressString(query) { placemarks, _ ->
                if (continuation.isActive) {
                    val placemark = placemarks?.firstOrNull() as? CLPlacemark
                    continuation.resume(placemark?.toCoordinates())
                }
            }
        }
    }

    private fun CLPlacemark.toCoordinates(): Coordinates? {
        val clLocation = location ?: return null
        return clLocation.coordinate.useContents {
            Coordinates(latitude = latitude, longitude = longitude)
        }
    }
}
