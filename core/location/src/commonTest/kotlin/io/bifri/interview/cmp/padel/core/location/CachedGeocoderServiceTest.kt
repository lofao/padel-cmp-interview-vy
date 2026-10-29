package io.bifri.interview.cmp.padel.core.location

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CachedGeocoderServiceTest {

    @Test
    fun geocode_cachesResultsAndAvoidsDuplicateDelegateCalls() = runBlocking {
        val delegate = CountingGeocoderService(
            mapOf("madrid|es" to Coordinates(40.4168, -3.7038)),
        )
        val cachedGeocoder = CachedGeocoderService(delegate)

        val firstCall = cachedGeocoder.geocode("Madrid", "ES")
        assertEquals(Coordinates(40.4168, -3.7038), firstCall)
        assertEquals(1, delegate.callCount)

        val secondCall = cachedGeocoder.geocode("Madrid", "ES")
        assertEquals(Coordinates(40.4168, -3.7038), secondCall)
        assertEquals(1, delegate.callCount)
    }

    @Test
    fun geocodeBatch_deDuplicatesAndGeocodesConcurrently() = runBlocking {
        val delegate = CountingGeocoderService(
            mapOf(
                "madrid|es" to Coordinates(40.4168, -3.7038),
                "barcelona|es" to Coordinates(41.3851, 2.1734),
            ),
        )
        val cachedGeocoder = CachedGeocoderService(delegate)

        val batch = listOf(
            "Madrid" to "ES",
            "Barcelona" to "ES",
            "Madrid" to "ES", // Duplicate in input batch
        )

        val result = cachedGeocoder.geocodeBatch(batch)
        assertEquals(2, result.size)
        assertEquals(Coordinates(40.4168, -3.7038), result["madrid|es"])
        assertEquals(Coordinates(41.3851, 2.1734), result["barcelona|es"])
        assertEquals(2, delegate.callCount)

        // Subsequent batch with same locations should use cache
        val secondBatchResult = cachedGeocoder.geocodeBatch(batch)
        assertEquals(2, secondBatchResult.size)
        assertEquals(2, delegate.callCount)
    }

    @Test
    fun geocode_blankQuery_returnsNull() = runBlocking {
        val delegate = CountingGeocoderService(emptyMap())
        val cachedGeocoder = CachedGeocoderService(delegate)

        val result = cachedGeocoder.geocode(" ", "")
        assertNull(result)
        assertEquals(0, delegate.callCount)
    }

    private class CountingGeocoderService(
        private val coordinatesMap: Map<String, Coordinates>,
    ) : GeocoderService {
        var callCount = 0
            private set

        override suspend fun geocode(location: String?, country: String?): Coordinates? {
            callCount++
            val key = listOfNotNull(location, country)
                .map { it.trim().lowercase() }
                .joinToString("|")
            return coordinatesMap[key]
        }
    }
}
