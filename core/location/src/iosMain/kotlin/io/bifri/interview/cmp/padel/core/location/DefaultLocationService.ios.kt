@file:Suppress("MagicNumber", "TooGenericExceptionCaught")

package io.bifri.interview.cmp.padel.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSOperationQueue
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class DefaultLocationService : LocationService {
    override fun observeLocation(): Flow<Coordinates?> = callbackFlow {
        var locationManager: CLLocationManager? = null

        withContext(Dispatchers.Main) {
            val manager = CLLocationManager()
            locationManager = manager
            manager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
            manager.distanceFilter = DistanceFilterM

            var lastEmittedTimeMs = 0L

            fun emitLocationIfAllowed(location: CLLocation?) {
                if (location == null) {
                    trySend(null)
                    return
                }
                val currentTimeMs = (NSDate().timeIntervalSince1970 * 1000).toLong()
                if (lastEmittedTimeMs == 0L || (currentTimeMs - lastEmittedTimeMs) >= MinTimeMs) {
                    lastEmittedTimeMs = currentTimeMs
                    val coords = location.coordinate.useContents {
                        Coordinates(latitude = latitude, longitude = longitude)
                    }
                    trySend(coords)
                }
            }

            fun handleAuthorizationStatus(status: CLAuthorizationStatus) {
                when (status) {
                    kCLAuthorizationStatusAuthorizedWhenInUse,
                    kCLAuthorizationStatusAuthorizedAlways,
                    -> {
                        manager.startUpdatingLocation()
                        emitLocationIfAllowed(manager.location)
                    }

                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted,
                    -> {
                        manager.stopUpdatingLocation()
                        trySend(null)
                    }

                    kCLAuthorizationStatusNotDetermined -> {
                        manager.requestWhenInUseAuthorization()
                        trySend(null)
                    }

                    else -> {
                        trySend(null)
                    }
                }
            }

            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    emitLocationIfAllowed(location)
                }

                @Suppress("UnusedParameter")
                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    trySend(null)
                }

                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    handleAuthorizationStatus(manager.authorizationStatus)
                }

                @Suppress("UnusedParameter")
                override fun locationManager(
                    manager: CLLocationManager,
                    didChangeAuthorizationStatus: CLAuthorizationStatus,
                ) {
                    handleAuthorizationStatus(didChangeAuthorizationStatus)
                }
            }

            manager.delegate = delegate
            handleAuthorizationStatus(manager.authorizationStatus)
        }

        awaitClose {
            NSOperationQueue.mainQueue.addOperationWithBlock {
                locationManager?.stopUpdatingLocation()
                locationManager?.delegate = null
                locationManager = null
            }
        }
    }

    companion object {
        private const val DistanceFilterM = 10.0
        private const val MinTimeMs = 60000L
    }
}
