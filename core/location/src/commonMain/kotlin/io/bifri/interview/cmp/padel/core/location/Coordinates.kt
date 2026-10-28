package io.bifri.interview.cmp.padel.core.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    fun distanceToKm(other: Coordinates): Double {
        val lat1Rad = Math.toRadians(latitude)
        val lat2Rad = Math.toRadians(other.latitude)
        val deltaLatRad = Math.toRadians(other.latitude - latitude)
        val deltaLonRad = Math.toRadians(other.longitude - longitude)

        val sinDeltaLat = sin(deltaLatRad / 2)
        val sinDeltaLon = sin(deltaLonRad / 2)

        val a = sinDeltaLat * sinDeltaLat +
            cos(lat1Rad) * cos(lat2Rad) * sinDeltaLon * sinDeltaLon

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EarthRadiusKm * c
    }

    companion object {
        private const val EarthRadiusKm = 6371.0
    }
}
