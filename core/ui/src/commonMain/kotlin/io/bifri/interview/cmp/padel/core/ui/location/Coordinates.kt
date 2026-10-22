package io.bifri.interview.cmp.padel.core.ui.location

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EarthRadiusKm = 6371.0
private const val HalfCircleDegrees = 180.0

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    fun distanceToKm(other: Coordinates): Double {
        val dLat = (other.latitude - latitude).toRadians()
        val dLon = (other.longitude - longitude).toRadians()
        val a = sin(dLat / 2.0).pow(2.0) +
            cos(latitude.toRadians()) * cos(other.latitude.toRadians()) * sin(dLon / 2.0).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EarthRadiusKm * c
    }

    private fun Double.toRadians(): Double = this * (PI / HalfCircleDegrees)
}
