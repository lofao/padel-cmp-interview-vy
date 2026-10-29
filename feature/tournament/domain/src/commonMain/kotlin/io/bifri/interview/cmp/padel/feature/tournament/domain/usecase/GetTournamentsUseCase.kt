package io.bifri.interview.cmp.padel.feature.tournament.domain.usecase

import co.touchlab.kermit.Logger
import io.bifri.interview.cmp.padel.core.location.Coordinates
import io.bifri.interview.cmp.padel.core.location.GeocoderService
import io.bifri.interview.cmp.padel.core.location.LocationService
import io.bifri.interview.cmp.padel.feature.tournament.data.repository.TournamentRepository
import io.bifri.interview.cmp.padel.feature.tournament.domain.mapper.TournamentsDomainMapper
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournament
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournamentWithDistance
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.measureTimedValue

/**
 * Use case to retrieve tournaments sorted by distance to the user's current location.
 *
 * Performance Optimizations:
 * 1. In-memory Geocode Cache: Caches resolved coordinates by location key (location + country) across
 *    location updates to avoid re-geocoding identical addresses repeatedly on every GPS stream update.
 * 2. Parallel Geocoding Execution: Performs geocoding for missing unique locations concurrently using
 *    [coroutineScope] and [async] / [awaitAll], reducing latency from O(N * T) to O(T_max).
 * 3. De-duplication: Extracts distinct location keys so multiple tournaments in the same city are geocoded
 *    at most once.
 * 4. Metrics & Logging: Measures execution time and logs performance metrics (cache hits/misses, duration).
 */
class GetTournamentsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val mapper: TournamentsDomainMapper,
    private val locationService: LocationService,
    private val geocoderService: GeocoderService,
) {
    private val logger = Logger.withTag("GetTournamentsUseCase")

    // Cache resolved coordinates to prevent redundant network/I/O geocoding calls on location updates.
    private val geocodeCache = mutableMapOf<String, Coordinates?>()
    private val cacheMutex = Mutex()

    operator fun invoke(): Flow<List<TournamentsTournamentWithDistance>> = combine(
        flow { emit(with(mapper) { tournamentRepository.getTournaments().toDomain() }) },
        locationService.observeLocation().distinctUntilChanged { old, new ->
            areLocationsEquivalent(old, new)
        },
        ::processTournamentsWithLocation,
    )

    private fun areLocationsEquivalent(old: Coordinates?, new: Coordinates?): Boolean {
        if (old == null && new == null) return true
        if (old == null || new == null) return false
        // Ignore minor GPS noise/drift (< 50 meters = 0.05 km)
        return old.distanceToKm(new) < MinimumDistanceChangeThresholdKm
    }

    private suspend fun processTournamentsWithLocation(
        tournaments: List<TournamentsTournament>,
        location: Coordinates?,
    ): List<TournamentsTournamentWithDistance> {
        val (result, duration) = measureTimedValue {
            if (location == null) {
                return@measureTimedValue tournaments.map { TournamentsTournamentWithDistance(tournament = it) }
            }

            // Optimization 1: Identify distinct non-blank location keys to avoid duplicate geocoding calls.
            val uniqueLocations = tournaments.mapNotNull { tournament ->
                val key = buildLocationKey(tournament.location, tournament.country)
                if (key.isBlank()) null else key to (tournament.location to tournament.country)
            }.toMap()

            // Optimization 2: Check cache under lock and find missing location keys.
            val missingLocations = cacheMutex.withLock {
                uniqueLocations.filterKeys { !geocodeCache.containsKey(it) }
            }

            if (missingLocations.isNotEmpty()) {
                // Optimization 3: Geocode missing unique locations concurrently using async/awaitAll.
                val newCoordinates = coroutineScope {
                    missingLocations.map { (key, locPair) ->
                        async {
                            val (loc, country) = locPair
                            val coords = geocoderService.geocode(loc, country)
                            key to coords
                        }
                    }.awaitAll()
                }

                // Optimization 4: Store newly geocoded coordinates in thread-safe cache.
                cacheMutex.withLock {
                    for ((key, coords) in newCoordinates) {
                        geocodeCache[key] = coords
                    }
                }
            }

            // Optimization 5: Read cached coordinates snapshot for fast distance calculation.
            val currentCacheSnapshot = cacheMutex.withLock { geocodeCache.toMap() }

            val items = tournaments.map { tournament ->
                val key = buildLocationKey(tournament.location, tournament.country)
                val coords = currentCacheSnapshot[key]
                val distance = coords?.let { location.distanceToKm(it) }
                TournamentsTournamentWithDistance(
                    tournament = tournament,
                    distanceKm = distance,
                )
            }

            items.sortedWith(compareBy { it.distanceKm ?: Double.MAX_VALUE })
        }

        // Performance Metrics Logging
        logger.d {
            "Processed ${tournaments.size} tournaments in ${duration.inWholeMilliseconds} ms " +
                "(Location: ${location != null}, Unique locations: ${
                    tournaments.map { buildLocationKey(it.location, it.country) }.distinct().size
                })"
        }

        return result
    }

    private fun buildLocationKey(location: String?, country: String?): String {
        val locStr = location?.trim()?.lowercase().orEmpty()
        val countryStr = country?.trim()?.lowercase().orEmpty()
        return "$locStr|$countryStr"
    }

    companion object {
        private const val MinimumDistanceChangeThresholdKm = 0.05
    }
}
