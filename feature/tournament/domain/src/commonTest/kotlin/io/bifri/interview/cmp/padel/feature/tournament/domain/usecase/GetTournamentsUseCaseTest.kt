package io.bifri.interview.cmp.padel.feature.tournament.domain.usecase

import io.bifri.interview.cmp.padel.core.exception.ExceptionHandler
import io.bifri.interview.cmp.padel.core.extensions.datetime.DateTimeUtils
import io.bifri.interview.cmp.padel.core.location.Coordinates
import io.bifri.interview.cmp.padel.core.location.GeocoderService
import io.bifri.interview.cmp.padel.core.location.LocationService
import io.bifri.interview.cmp.padel.feature.tournament.data.datasource.model.TournamentResponseDto
import io.bifri.interview.cmp.padel.feature.tournament.data.datasource.model.TournamentsResponseDto
import io.bifri.interview.cmp.padel.feature.tournament.data.datasource.model.TournamentsResponseDto.TournamentDto
import io.bifri.interview.cmp.padel.feature.tournament.data.repository.TournamentRepository
import io.bifri.interview.cmp.padel.feature.tournament.domain.mapper.TournamentsDomainMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTournamentsUseCaseTest {

    @Test
    fun invoke_sortsTournamentsByDistanceWhenLocationIsAvailable() = runBlocking {
        val repository = FakeTournamentRepository(
            tournaments = listOf(
                TournamentDto(
                    id = 1L,
                    name = "Tournament Barcelona",
                    location = "Barcelona",
                    country = "ES",
                ),
                TournamentDto(
                    id = 2L,
                    name = "Tournament Madrid",
                    location = "Madrid",
                    country = "ES",
                ),
            ),
        )

        val mapper = TournamentsDomainMapper(
            exceptionHandler = FakeExceptionHandler(),
            dateTimeUtils = DateTimeUtils(),
        )

        val madridCoords = Coordinates(40.4168, -3.7038)
        val locationService = FakeLocationService(madridCoords)
        val geocoderService = FakeGeocoderService(
            mapOf(
                "Madrid, ES" to Coordinates(40.4168, -3.7038),
                "Barcelona, ES" to Coordinates(41.3851, 2.1734),
            ),
        )

        val useCase = GetTournamentsUseCase(
            tournamentRepository = repository,
            mapper = mapper,
            locationService = locationService,
            geocoderService = geocoderService,
        )

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals("Tournament Madrid", result[0].name)
        assertEquals("Tournament Barcelona", result[1].name)
    }

    private class FakeTournamentRepository(
        private val tournaments: List<TournamentDto>,
    ) : TournamentRepository {
        override suspend fun getTournaments(): TournamentsResponseDto = TournamentsResponseDto(data = tournaments)

        override suspend fun getTournament(id: Long): TournamentResponseDto {
            error("Not implemented")
        }
    }

    private class FakeLocationService(
        private val location: Coordinates?,
    ) : LocationService {
        override fun observeLocation(): Flow<Coordinates?> = flowOf(location)
    }

    private class FakeGeocoderService(
        private val coordinatesMap: Map<String, Coordinates>,
    ) : GeocoderService {
        override suspend fun geocode(location: String?, country: String?): Coordinates? {
            val key = listOfNotNull(location, country).joinToString(", ")
            return coordinatesMap[key]
        }
    }

    private class FakeExceptionHandler : ExceptionHandler {
        override fun handleException(throwable: Throwable) {
            // No-op for testing
        }
    }
}
