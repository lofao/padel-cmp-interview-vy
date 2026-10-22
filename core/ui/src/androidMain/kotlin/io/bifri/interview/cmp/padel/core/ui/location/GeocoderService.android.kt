package io.bifri.interview.cmp.padel.core.ui.location

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

actual class GeocoderService(
    private val context: Context,
) {
    actual suspend fun geocode(location: String?, country: String?): Coordinates? {
        val query = listOfNotNull(location, country).filter { it.isNotBlank() }.joinToString(", ")
        if (query.isBlank()) return null

        val queryKey = query.lowercase()
        val locationKey = location?.lowercase().orEmpty()
        val fallback = FallbackCoordinates[queryKey] ?: FallbackCoordinates[locationKey]

        return withContext(Dispatchers.IO) {
            try {
                if (fallback != null) return@withContext fallback

                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    Coordinates(
                        latitude = addresses[0].latitude,
                        longitude = addresses[0].longitude,
                    )
                } else {
                    fallback
                }
            } catch (_: Exception) {
                fallback
            }
        }
    }
}
