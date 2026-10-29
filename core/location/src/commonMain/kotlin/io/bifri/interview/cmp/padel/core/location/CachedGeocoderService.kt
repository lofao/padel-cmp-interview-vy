package io.bifri.interview.cmp.padel.core.location

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Decorates [GeocoderService] to add in-memory caching and batch geocoding support.
 */
class CachedGeocoderService(
    private val delegate: GeocoderService,
) : GeocoderService {

    private val cache = mutableMapOf<String, Coordinates?>()
    private val mutex = Mutex()

    override suspend fun geocode(location: String?, country: String?): Coordinates? {
        if (location.isNullOrBlank() && country.isNullOrBlank()) return null
        val key = buildLocationKey(location, country)

        mutex.withLock {
            if (cache.containsKey(key)) {
                return cache[key]
            }
        }

        val result = delegate.geocode(location, country)
        mutex.withLock {
            cache[key] = result
        }
        return result
    }

    /**
     * Batch geocodes multiple location/country pairs, returning a map of location keys to resolved coordinates.
     * De-duplicates unique locations and geocodes missing entries concurrently using async/awaitAll.
     */
    suspend fun geocodeBatch(locations: List<Pair<String?, String?>>): Map<String, Coordinates?> {
        val uniqueLocations = locations
            .mapNotNull { (location, country) ->
                val key = buildLocationKey(location, country)
                if (key.isBlank()) null else key to (location to country)
            }
            .toMap()

        if (uniqueLocations.isEmpty()) return emptyMap()

        val missingLocations = mutex.withLock {
            uniqueLocations.filterKeys { !cache.containsKey(it) }
        }

        if (missingLocations.isNotEmpty()) {
            val newEntries = coroutineScope {
                missingLocations.map { (key, locPair) ->
                    async {
                        val (loc, country) = locPair
                        val coords = delegate.geocode(loc, country)
                        key to coords
                    }
                }.awaitAll()
            }

            mutex.withLock {
                for ((key, coords) in newEntries) {
                    cache[key] = coords
                }
            }
        }

        val snapshot = mutex.withLock { cache.toMap() }
        return uniqueLocations.keys.associateWith { snapshot[it] }
    }

    fun buildLocationKey(location: String?, country: String?): String {
        val locStr = location?.trim()?.lowercase().orEmpty()
        val countryStr = country?.trim()?.lowercase().orEmpty()
        if (locStr.isBlank() && countryStr.isBlank()) return ""
        return "$locStr|$countryStr"
    }
}
