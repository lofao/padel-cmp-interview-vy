package io.bifri.interview.cmp.padel.feature.tournament.domain.usecase

import co.touchlab.kermit.Logger
import io.bifri.interview.cmp.padel.core.location.CachedGeocoderService
import io.bifri.interview.cmp.padel.core.location.Coordinates
import io.bifri.interview.cmp.padel.core.location.LocationService
import io.bifri.interview.cmp.padel.feature.tournament.data.repository.TournamentRepository
import io.bifri.interview.cmp.padel.feature.tournament.domain.mapper.TournamentsDomainMapper
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournament
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournamentWithDistance
import io.bifri.interview.cmp.padel.feature.tournament.domain.util.TournamentDistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlin.time.measureTimedValue

/**
 * Use case to retrieve tournaments sorted by distance to the user's current location.
 */
class GetTournamentsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val mapper: TournamentsDomainMapper,
    private val locationService: LocationService,
    private val geocoderService: CachedGeocoderService,
) {
    private val logger = Logger.withTag("GetTournamentsUseCase")

    operator fun invoke(): Flow<List<TournamentsTournamentWithDistance>> = combine(
        flow { emit(with(mapper) { tournamentRepository.getTournaments().toDomain() }) },
        locationService.observeLocation().distinctUntilChanged { old, new ->
            !TournamentDistanceCalculator.isLocationChangeSignificant(old, new)
        },
        ::processTournamentsWithLocation,
    )

    private suspend fun processTournamentsWithLocation(
        tournaments: List<TournamentsTournament>,
        location: Coordinates?,
    ): List<TournamentsTournamentWithDistance> {
        val (result, duration) = measureTimedValue {
            if (location == null) {
                return@measureTimedValue tournaments.map { TournamentsTournamentWithDistance(tournament = it) }
            }

            val locations = tournaments.map { it.location to it.country }
            val geocodedMap = geocoderService.geocodeBatch(locations)

            TournamentDistanceCalculator.sortByDistance(
                tournaments = tournaments,
                userLocation = location,
                geocodedCoordinatesMap = geocodedMap,
                locationKeyBuilder = geocoderService::buildLocationKey,
            )
        }

        logger.d {
            "Processed ${tournaments.size}" +
                " tournaments in ${duration.inWholeMilliseconds} ms (Location: ${location != null})"
        }

        return result
    }
}
