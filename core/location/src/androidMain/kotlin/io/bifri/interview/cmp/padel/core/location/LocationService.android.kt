package io.bifri.interview.cmp.padel.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual class LocationService(
    private val context: Context,
) {
    @SuppressLint("MissingPermission")
    actual fun observeLocation(): Flow<Coordinates?> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        if (locationManager == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = createLocationListener()
        val lastLocationSent = sendLastKnownLocation(locationManager)

        requestLocationUpdates(locationManager, listener, lastLocationSent)

        awaitClose {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun ProducerScope<Coordinates?>.createLocationListener() = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            trySend(Coordinates(location.latitude, location.longitude))
        }

        @Deprecated("Deprecated in API 29")
        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {
            // Deprecated callback required for older API levels
        }
    }

    @SuppressLint("MissingPermission")
    private fun ProducerScope<Coordinates?>.sendLastKnownLocation(locationManager: LocationManager): Boolean = try {
        val lastKnownGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastKnownNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val bestLastLocation = lastKnownGps ?: lastKnownNet
        if (bestLastLocation != null) {
            trySend(Coordinates(bestLastLocation.latitude, bestLastLocation.longitude))
            true
        } else {
            false
        }
    } catch (_: SecurityException) {
        false
    } catch (_: Exception) {
        false
    }

    @SuppressLint("MissingPermission")
    private fun ProducerScope<Coordinates?>.requestLocationUpdates(
        locationManager: LocationManager,
        listener: LocationListener,
        lastLocationSent: Boolean,
    ) {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MinTimeMs,
                    MinDistanceM,
                    listener,
                )
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MinTimeMs,
                    MinDistanceM,
                    listener,
                )
            } else if (!lastLocationSent) {
                trySend(null)
            }
        } catch (_: SecurityException) {
            trySend(null)
        } catch (_: Exception) {
            if (!lastLocationSent) {
                trySend(null)
            }
        }
    }

    companion object {
        private const val MinTimeMs = 60000L
        private const val MinDistanceM = 10f
    }
}
