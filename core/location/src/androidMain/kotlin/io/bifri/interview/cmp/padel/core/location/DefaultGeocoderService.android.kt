package io.bifri.interview.cmp.padel.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class DefaultGeocoderService(
    private val context: Context,
) : BaseGeocoderService() {
    private val logger = Logger.withTag("DefaultGeocoderService")

    @Suppress("TooGenericExceptionCaught")
    override suspend fun geocodePlatform(query: String): Coordinates? = try {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocodeTiramisu(geocoder, query)
        } else {
            geocodeLegacy(geocoder, query)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.w(e) { "Geocoding failed for query: $query" }
        null
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun geocodeTiramisu(geocoder: Geocoder, query: String): Coordinates? =
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocationName(
                query,
                MaxResults,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (continuation.isActive) {
                            continuation.resume(addresses.firstOrNull()?.toCoordinates())
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                },
            )
        }

    @Suppress("DEPRECATION")
    private suspend fun geocodeLegacy(geocoder: Geocoder, query: String): Coordinates? =
        withContext(Dispatchers.IO) {
            geocoder.getFromLocationName(query, MaxResults)
                ?.firstOrNull()
                ?.toCoordinates()
        }

    private fun Address.toCoordinates() = Coordinates(latitude = latitude, longitude = longitude)

    companion object {
        private const val MaxResults = 1
    }
}
