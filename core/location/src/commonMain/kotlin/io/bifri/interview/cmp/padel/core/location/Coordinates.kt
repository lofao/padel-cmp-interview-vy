package io.bifri.interview.cmp.padel.core.location

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    @Suppress("MagicNumber")
    fun distanceToKm(other: Coordinates): Double {
        val lat1Rad = latitude.toRadians()
        val lat2Rad = other.latitude.toRadians()
        val deltaLatRad = (other.latitude - latitude).toRadians()
        val deltaLonRad = (other.longitude - longitude).toRadians()

        val sinDeltaLat = sin(deltaLatRad / 2)
        val sinDeltaLon = sin(deltaLonRad / 2)

        val a = (sinDeltaLat * sinDeltaLat) +
            (cos(lat1Rad) * cos(lat2Rad) * sinDeltaLon * sinDeltaLon)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EarthRadiusKm * c
    }

    @Suppress("MagicNumber")
    private fun Double.toRadians(): Double = this * PI / 180.0

    companion object {
        private const val EarthRadiusKm = 6371.0
    }
}
