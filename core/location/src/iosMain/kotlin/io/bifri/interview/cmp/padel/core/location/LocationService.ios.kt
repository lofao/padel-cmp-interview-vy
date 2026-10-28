package io.bifri.interview.cmp.padel.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class LocationService {
    actual fun observeLocation(): Flow<Coordinates?> = callbackFlow {
        val locationManager = CLLocationManager()
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        locationManager.distanceFilter = DistanceFilterM

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.lastOrNull() as? CLLocation
                if (location != null) {
                    val coords = location.coordinate.useContents {
                        Coordinates(latitude = latitude, longitude = longitude)
                    }
                    trySend(coords)
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                trySend(null)
            }
        }

        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()

        val lastLoc = locationManager.location
        if (lastLoc != null) {
            val coords = lastLoc.coordinate.useContents {
                Coordinates(latitude = latitude, longitude = longitude)
            }
            trySend(coords)
        } else {
            trySend(null)
        }

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
        }
    }

    companion object {
        private const val DistanceFilterM = 10.0
    }
}
