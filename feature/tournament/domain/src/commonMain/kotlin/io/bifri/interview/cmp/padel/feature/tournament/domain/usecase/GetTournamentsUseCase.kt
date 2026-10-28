package io.bifri.interview.cmp.padel.feature.tournament.domain.usecase

import io.bifri.interview.cmp.padel.core.location.Coordinates
import io.bifri.interview.cmp.padel.core.location.GeocoderService
import io.bifri.interview.cmp.padel.core.location.LocationService
import io.bifri.interview.cmp.padel.feature.tournament.data.repository.TournamentRepository
import io.bifri.interview.cmp.padel.feature.tournament.domain.mapper.TournamentsDomainMapper
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournament
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournamentWithDistance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class GetTournamentsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val mapper: TournamentsDomainMapper,
    private val locationService: LocationService,
    private val geocoderService: GeocoderService,
) {
    operator fun invoke(): Flow<List<TournamentsTournamentWithDistance>> = combine(
        flow { emit(with(mapper) { tournamentRepository.getTournaments().toDomain() }) },
        locationService.observeLocation().distinctUntilChanged(),
        ::processTournamentsWithLocation,
    )

    private suspend fun processTournamentsWithLocation(
        tournaments: List<TournamentsTournament>,
        location: Coordinates?,
    ): List<TournamentsTournamentWithDistance> {
        if (location == null) {
            return tournaments.map { TournamentsTournamentWithDistance(tournament = it) }
        }

        val items = tournaments.map { tournament ->
            val coords = geocoderService.geocode(tournament.location, tournament.country)
            val distance = coords?.let { location.distanceToKm(it) }
            TournamentsTournamentWithDistance(
                tournament = tournament,
                distanceKm = distance,
            )
        }

        return items.sortedWith(
            compareBy { it.distanceKm ?: Double.MAX_VALUE },
        )
    }
}
