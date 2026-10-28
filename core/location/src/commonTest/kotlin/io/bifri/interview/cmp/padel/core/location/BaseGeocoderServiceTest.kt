package io.bifri.interview.cmp.padel.core.location

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BaseGeocoderServiceTest {

    @Test
    fun geocode_blankQuery_returnsNullWithoutCallingPlatform() = runBlocking {
        val service = TestGeocoderService()
        val result = service.geocode(" ", " ")
        assertNull(result)
        assertEquals(0, service.platformCalls)
    }

    @Test
    fun geocode_knownFallback_returnsFallbackWithoutCallingPlatform() = runBlocking {
        val service = TestGeocoderService()
        val result = service.geocode("Madrid", "ES")
        assertEquals(Coordinates(MadridLat, MadridLon), result)
        assertEquals(0, service.platformCalls)
    }

    @Test
    fun geocode_unknownLocation_callsPlatformGeocoder() = runBlocking {
        val service = TestGeocoderService(
            mockPlatformResult = Coordinates(TestLat, TestLon),
        )
        val result = service.geocode("Unknown City", "XX")
        assertEquals(Coordinates(TestLat, TestLon), result)
        assertEquals(1, service.platformCalls)
    }

    private class TestGeocoderService(
        private val mockPlatformResult: Coordinates? = null,
    ) : BaseGeocoderService() {
        var platformCalls = 0
            private set

        override suspend fun geocodePlatform(query: String): Coordinates? {
            platformCalls++
            return mockPlatformResult
        }
    }

    companion object {
        private const val MadridLat = 40.4168
        private const val MadridLon = -3.7038
        private const val TestLat = 10.0
        private const val TestLon = 20.0
    }
}
