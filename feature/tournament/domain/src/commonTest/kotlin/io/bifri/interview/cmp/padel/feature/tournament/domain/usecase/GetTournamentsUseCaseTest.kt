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
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournamentWithDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
        val geocoderService = TrackingGeocoderService(
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
        assertEquals("Tournament Madrid", result[0].tournament.name)
        assertEquals("Tournament Barcelona", result[1].tournament.name)
    }

    @Test
    fun invoke_cachesGeocodedLocationsAcrossLocationUpdates() = runBlocking {
        val repository = FakeTournamentRepository(
            tournaments = listOf(
                TournamentDto(id = 1L, name = "Barcelona 1", location = "Barcelona", country = "ES"),
                TournamentDto(id = 2L, name = "Barcelona 2", location = "Barcelona", country = "ES"),
                TournamentDto(id = 3L, name = "Madrid 1", location = "Madrid", country = "ES"),
            ),
        )

        val mapper = TournamentsDomainMapper(
            exceptionHandler = FakeExceptionHandler(),
            dateTimeUtils = DateTimeUtils(),
        )

        val locationFlow = MutableStateFlow<Coordinates?>(Coordinates(40.4168, -3.7038))
        val locationService = FlowLocationService(locationFlow)
        val geocoderService = TrackingGeocoderService(
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

        val firstResult = useCase().first()
        assertEquals("Madrid 1", firstResult[0].tournament.name)
        // Geocoder should be called exactly twice (once for Barcelona, once for Madrid) despite 3 tournaments
        assertEquals(2, geocoderService.callCount)

        // Update user location to Barcelona
        locationFlow.value = Coordinates(41.3851, 2.1734)

        val secondResult = useCase().first()
        assertEquals("Barcelona 1", secondResult[0].tournament.name)
        assertEquals("Barcelona 2", secondResult[1].tournament.name)
        // Geocoder call count should remain 2 because results were cached!
        assertEquals(2, geocoderService.callCount)
    }

    @Test
    fun invoke_ignoresMinorLocationJitter() = runBlocking {
        val repository = FakeTournamentRepository(
            tournaments = listOf(
                TournamentDto(id = 1L, name = "Madrid", location = "Madrid", country = "ES"),
            ),
        )

        val mapper = TournamentsDomainMapper(
            exceptionHandler = FakeExceptionHandler(),
            dateTimeUtils = DateTimeUtils(),
        )

        val initialCoords = Coordinates(40.4168000, -3.7038000)
        // Tiny offset (~1.1 meters)
        val jitterCoords = Coordinates(40.4168010, -3.7038000)

        val locationFlow = MutableStateFlow<Coordinates?>(initialCoords)
        val locationService = FlowLocationService(locationFlow)
        val geocoderService = TrackingGeocoderService(
            mapOf("Madrid, ES" to Coordinates(40.4168, -3.7038)),
        )

        val useCase = GetTournamentsUseCase(
            tournamentRepository = repository,
            mapper = mapper,
            locationService = locationService,
            geocoderService = geocoderService,
        )

        val emissions = mutableListOf<List<TournamentsTournamentWithDistance>>()
        val job = launch(Dispatchers.Unconfined) {
            useCase().collect { emissions.add(it) }
        }

        assertEquals(1, emissions.size)

        // Minor GPS noise (< 50m distance change) should be ignored
        locationFlow.value = jitterCoords
        assertEquals(1, emissions.size)

        // Significant location change (> 50m distance change) should trigger emission
        locationFlow.value = Coordinates(41.3851, 2.1734)
        assertEquals(2, emissions.size)

        job.cancel()
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

    private class FlowLocationService(
        private val locationFlow: Flow<Coordinates?>,
    ) : LocationService {
        override fun observeLocation(): Flow<Coordinates?> = locationFlow
    }

    private class TrackingGeocoderService(
        private val coordinatesMap: Map<String, Coordinates>,
    ) : GeocoderService {
        var callCount = 0
            private set

        override suspend fun geocode(location: String?, country: String?): Coordinates? {
            callCount++
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
