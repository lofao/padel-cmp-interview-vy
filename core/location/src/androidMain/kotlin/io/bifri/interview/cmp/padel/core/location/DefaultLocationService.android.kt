@file:Suppress("MagicNumber")

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
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Android implementation of [LocationService] using system [LocationManager].
 *
 * Key features:
 * - Emits real-time location changes as [Coordinates] inside a [callbackFlow].
 * - Handles runtime permission checks and re-evaluates location updates when permissions change.
 * - Listens for system provider state changes (GPS/Network enabled/disabled) via [BroadcastReceiver].
 * - Emits last known location fix immediately when available for fast initial sorting.
 * - Safely unregisters location listeners and broadcast receivers when the flow collection stops.
 */
class DefaultLocationService(
    private val context: Context,
) : LocationService {
    private val logger = Logger.withTag("DefaultLocationService")

    // Event trigger used when runtime location permissions are granted or updated.
    private val permissionTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    /**
     * Notifies the location service that location permissions were updated in the UI layer.
     */
    override fun onPermissionUpdated() {
        permissionTrigger.tryEmit(Unit)
    }

    /**
     * Observes current device location updates as a cold [Flow] of nullable [Coordinates].
     * Emits `null` if location services are disabled, unavailable, or permission is denied.
     */
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

        // Attempts to start updates if permission is granted and updates are not already active.
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

        // Perform initial check on subscription
        tryStartUpdates()

        // Register receiver for provider state toggles (e.g. user toggling GPS on/off in quick settings)
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

        // React to runtime permission updates from the UI
        val permissionJob = permissionTrigger.onEach {
            tryStartUpdates()
        }.launchIn(this)

        // Clean up listeners and broadcast receivers when flow collector cancels
        awaitClose {
            permissionJob.cancel()
            try {
                context.unregisterReceiver(providerReceiver)
            } catch (e: IllegalArgumentException) {
                logger.w(e) { "Receiver was not registered during cleanup" }
            }
            if (isListenerRegistered) {
                try {
                    locationManager.removeUpdates(listener)
                } catch (e: SecurityException) {
                    logger.w(e) { "Security exception removing location updates" }
                } catch (e: IllegalArgumentException) {
                    logger.w(e) { "Listener was not registered when removing updates" }
                }
            }
        }
    }

    // Checks whether fine or coarse location permission is granted.
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

    // Creates a LocationListener that forwards incoming Android Location objects to the callback flow.
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

    // Checks GPS and Network providers for cached last known location fixes to emit immediately.
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
    } catch (e: SecurityException) {
        logger.w(e) { "Security exception obtaining last known location" }
        false
    } catch (e: IllegalArgumentException) {
        logger.w(e) { "Invalid provider for last known location" }
        false
    }

    // Requests continuous location updates using GPS provider if available, or Network provider as fallback.
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
    } catch (e: SecurityException) {
        logger.w(e) { "Security exception requesting location updates" }
        if (!lastLocationSent) {
            trySend(null)
        }
        false
    } catch (e: IllegalArgumentException) {
        logger.w(e) { "Invalid provider or listener for location updates" }
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
