@file:Suppress("MagicNumber")

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

/**
 * iOS implementation of [LocationService] using Apple's CoreLocation framework ([CLLocationManager]).
 *
 * Key features:
 * - Wraps CoreLocation callbacks inside Kotlin Coroutines [callbackFlow].
 * - Configures `CLLocationManager` on [Dispatchers.Main] as required by iOS CoreLocation framework.
 * - Handles authorization status state changes (`AuthorizedWhenInUse`, `Denied`, `NotDetermined`).
 * - Applies 10-meter distance filtering and rate-limits location emissions to [MinTimeMs] (1 minute).
 * - Safely stops location updates and releases delegate on main queue when flow is cancelled.
 */
@OptIn(ExperimentalForeignApi::class)
class DefaultLocationService : LocationService {

    /**
     * Observes current device location updates as a cold [Flow] of nullable [Coordinates].
     * Emits `null` if permission is denied, restricted, or when location updates fail.
     */
    override fun observeLocation(): Flow<Coordinates?> = callbackFlow {
        var locationManager: CLLocationManager? = null

        withContext(Dispatchers.Main) {
            val manager = CLLocationManager().apply {
                desiredAccuracy = kCLLocationAccuracyNearestTenMeters
                distanceFilter = DistanceFilterM
            }
            locationManager = manager

            var lastEmittedTimeMs = 0L

            fun emitLocationIfAllowed(location: CLLocation?) {
                if (location == null) {
                    trySend(null)
                    return
                }

                val currentTimeMs = (NSDate().timeIntervalSince1970 * 1000).toLong()
                val isTimeThresholdMet = (lastEmittedTimeMs == 0L) ||
                    ((currentTimeMs - lastEmittedTimeMs) >= MinTimeMs)

                if (isTimeThresholdMet) {
                    lastEmittedTimeMs = currentTimeMs
                    trySend(location.toCoordinates())
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

                    else -> trySend(null)
                }
            }

            manager.delegate = createLocationDelegate(
                onLocationUpdate = ::emitLocationIfAllowed,
                onAuthorizationChanged = { handleAuthorizationStatus(manager.authorizationStatus) },
                onError = { trySend(null) },
            )

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

    private fun createLocationDelegate(
        onLocationUpdate: (CLLocation?) -> Unit,
        onAuthorizationChanged: () -> Unit,
        onError: () -> Unit,
    ): CLLocationManagerDelegateProtocol = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation
            onLocationUpdate(location)
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            onError()
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            onAuthorizationChanged()
        }

        override fun locationManager(
            manager: CLLocationManager,
            didChangeAuthorizationStatus: CLAuthorizationStatus,
        ) {
            onAuthorizationChanged()
        }
    }

    private fun CLLocation.toCoordinates(): Coordinates = coordinate.useContents {
        Coordinates(latitude = latitude, longitude = longitude)
    }

    companion object {
        private const val DistanceFilterM = 10.0
        private const val MinTimeMs = 60000L
    }
}
