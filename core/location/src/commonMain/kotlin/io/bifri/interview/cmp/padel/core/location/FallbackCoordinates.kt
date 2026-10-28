package io.bifri.interview.cmp.padel.core.location

internal val FallbackCoordinates: Map<String, Coordinates> = mapOf(
    "london" to Coordinates(51.5074, -0.1278),
    "london, gb" to Coordinates(51.5074, -0.1278),
    "madrid" to Coordinates(40.4168, -3.7038),
    "madrid, es" to Coordinates(40.4168, -3.7038),
    "barcelona" to Coordinates(41.3851, 2.1734),
    "barcelona, es" to Coordinates(41.3851, 2.1734),
    "bali" to Coordinates(-8.4095, 115.1889),
    "bali, id" to Coordinates(-8.4095, 115.1889),
    "rome" to Coordinates(41.9028, 12.4964),
    "rome, it" to Coordinates(41.9028, 12.4964),
    "paris" to Coordinates(48.8566, 2.3522),
    "paris, fr" to Coordinates(48.8566, 2.3522),
    "buenos aires" to Coordinates(-34.6037, -58.3816),
    "buenos aires, ar" to Coordinates(-34.6037, -58.3816),
)
