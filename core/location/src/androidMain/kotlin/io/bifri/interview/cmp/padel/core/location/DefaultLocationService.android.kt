@file:Suppress("MagicNumber", "TooGenericExceptionCaught", "SwallowedException")

package io.bifri.interview.cmp.padel.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DefaultLocationService(
    private val context: Context,
) : LocationService {
    private val permissionTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    override fun onPermissionUpdated() {
        permissionTrigger.tryEmit(Unit)
    }

    @SuppressLint("MissingPermission")
    override fun observeLocation(): Flow<Coordinates?> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        if (locationManager == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var isListenerRegistered = false
        val listener = createLocationListener()

        fun tryStartUpdates() {
            if (isListenerRegistered) return
            if (!context.hasLocationPermission()) {
                trySend(null)
                return
            }

            val lastLocationSent = sendLastKnownLocation(locationManager)
            if (requestLocationUpdates(locationManager, listener, lastLocationSent)) {
                isListenerRegistered = true
            }
        }

        // Try initially
        tryStartUpdates()

        // Register broadcast receiver for location provider state changes
        val providerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                tryStartUpdates()
            }
        }
        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        ContextCompat.registerReceiver(
            context,
            providerReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        // React to permission grant/updates
        val permissionJob = permissionTrigger.onEach {
            tryStartUpdates()
        }.launchIn(this)

        awaitClose {
            permissionJob.cancel()
            try {
                context.unregisterReceiver(providerReceiver)
            } catch (_: Exception) {
                // Ignore cleanup errors
            }
            if (isListenerRegistered) {
                try {
                    locationManager.removeUpdates(listener)
                } catch (_: Exception) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    private fun Context.hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun ProducerScope<Coordinates?>.createLocationListener() = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            trySend(Coordinates(location.latitude, location.longitude))
        }

        @Deprecated("Deprecated in API 29")
        @Suppress("EmptyFunctionBlock")
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
    ): Boolean = try {
        when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MinTimeMs,
                    MinDistanceM,
                    listener,
                    Looper.getMainLooper(),
                )
                true
            }

            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MinTimeMs,
                    MinDistanceM,
                    listener,
                    Looper.getMainLooper(),
                )
                true
            }

            else -> {
                if (!lastLocationSent) {
                    trySend(null)
                }
                false
            }
        }
    } catch (_: SecurityException) {
        if (!lastLocationSent) {
            trySend(null)
        }
        false
    } catch (_: Exception) {
        if (!lastLocationSent) {
            trySend(null)
        }
        false
    }

    companion object {
        private const val MinTimeMs = 60000L
        private const val MinDistanceM = 10f
    }
}
