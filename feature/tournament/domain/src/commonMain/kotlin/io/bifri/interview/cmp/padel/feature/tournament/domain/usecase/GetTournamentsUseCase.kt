package io.bifri.interview.cmp.padel.feature.tournament.domain.usecase

import co.touchlab.kermit.Logger
import io.bifri.interview.cmp.padel.feature.tournament.data.repository.TournamentRepository
import io.bifri.interview.cmp.padel.feature.tournament.data.repository.UserLocationRepository
import io.bifri.interview.cmp.padel.feature.tournament.domain.mapper.TournamentsDomainMapper
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.LocationCoordinates
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournament
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournamentWithDistance
import io.bifri.interview.cmp.padel.feature.tournament.domain.repository.TournamentGeocoder
import io.bifri.interview.cmp.padel.feature.tournament.domain.util.TournamentDistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.time.measureTimedValue

/**
 * Use case to retrieve tournaments sorted by distance to the user's current location.
 */
class GetTournamentsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val mapper: TournamentsDomainMapper,
    private val geocoder: TournamentGeocoder,
    private val userLocationRepository: UserLocationRepository? = null,
) {
    private val logger = Logger.withTag("GetTournamentsUseCase")

    operator fun invoke(
        userLocationFlow: Flow<LocationCoordinates?> = userLocationRepository?.observeUserLocation()?.map { coords ->
            coords?.let { LocationCoordinates(it.latitude, it.longitude) }
        } ?: flowOf(null),
    ): Flow<List<TournamentsTournamentWithDistance>> = combine(
        flow { emit(with(mapper) { tournamentRepository.getTournaments().toDomain() }) },
        userLocationFlow.distinctUntilChanged { old, new ->
            !TournamentDistanceCalculator.isLocationChangeSignificant(old, new)
        },
        ::processTournamentsWithLocation,
    )

    private suspend fun processTournamentsWithLocation(
        tournaments: List<TournamentsTournament>,
        location: LocationCoordinates?,
    ): List<TournamentsTournamentWithDistance> {
        val (result, duration) = measureTimedValue {
            if (location == null) {
                return@measureTimedValue tournaments.map { TournamentsTournamentWithDistance(tournament = it) }
            }

            val locations = tournaments.map { it.location to it.country }
            val geocodedMap = geocoder.geocodeBatch(locations)

            TournamentDistanceCalculator.sortByDistance(
                tournaments = tournaments,
                userLocation = location,
                geocodedCoordinatesMap = geocodedMap,
                locationKeyBuilder = geocoder::buildLocationKey,
            )
        }

        logger.d {
            "Processed ${tournaments.size}" +
                " tournaments in ${duration.inWholeMilliseconds} ms (Location: ${location != null})"
        }

        return result
    }
}
