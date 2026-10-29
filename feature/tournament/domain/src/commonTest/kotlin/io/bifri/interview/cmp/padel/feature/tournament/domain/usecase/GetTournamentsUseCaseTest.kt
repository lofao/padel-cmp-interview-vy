package io.bifri.interview.cmp.padel.feature.tournament.domain.usecase

import io.bifri.interview.cmp.padel.core.exception.ExceptionHandler
import io.bifri.interview.cmp.padel.core.extensions.datetime.DateTimeUtils
import io.bifri.interview.cmp.padel.feature.tournament.data.datasource.model.TournamentResponseDto
import io.bifri.interview.cmp.padel.feature.tournament.data.datasource.model.TournamentsResponseDto
import io.bifri.interview.cmp.padel.feature.tournament.data.datasource.model.TournamentsResponseDto.TournamentDto
import io.bifri.interview.cmp.padel.feature.tournament.data.repository.TournamentRepository
import io.bifri.interview.cmp.padel.feature.tournament.domain.mapper.TournamentsDomainMapper
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.LocationCoordinates
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournamentWithDistance
import io.bifri.interview.cmp.padel.feature.tournament.domain.repository.TournamentGeocoder
import kotlinx.coroutines.Dispatchers
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

        val madridCoords = LocationCoordinates(40.4168, -3.7038)
        val geocoder = TrackingTournamentGeocoder(
            mapOf(
                "madrid|es" to LocationCoordinates(40.4168, -3.7038),
                "barcelona|es" to LocationCoordinates(41.3851, 2.1734),
            ),
        )

        val useCase = GetTournamentsUseCase(
            tournamentRepository = repository,
            mapper = mapper,
            geocoder = geocoder,
        )

        val result = useCase(flowOf(madridCoords)).first()

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

        val locationFlow = MutableStateFlow<LocationCoordinates?>(LocationCoordinates(40.4168, -3.7038))
        val geocoder = TrackingTournamentGeocoder(
            mapOf(
                "madrid|es" to LocationCoordinates(40.4168, -3.7038),
                "barcelona|es" to LocationCoordinates(41.3851, 2.1734),
            ),
        )

        val useCase = GetTournamentsUseCase(
            tournamentRepository = repository,
            mapper = mapper,
            geocoder = geocoder,
        )

        val firstResult = useCase(locationFlow).first()
        assertEquals("Madrid 1", firstResult[0].tournament.name)
        assertEquals(1, geocoder.callCount)

        // Update user location to Barcelona
        locationFlow.value = LocationCoordinates(41.3851, 2.1734)

        val secondResult = useCase(locationFlow).first()
        assertEquals("Barcelona 1", secondResult[0].tournament.name)
        assertEquals("Barcelona 2", secondResult[1].tournament.name)
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

        val initialCoords = LocationCoordinates(40.4168000, -3.7038000)
        // Tiny offset (~1.1 meters)
        val jitterCoords = LocationCoordinates(40.4168010, -3.7038000)

        val locationFlow = MutableStateFlow<LocationCoordinates?>(initialCoords)
        val geocoder = TrackingTournamentGeocoder(
            mapOf("madrid|es" to LocationCoordinates(40.4168, -3.7038)),
        )

        val useCase = GetTournamentsUseCase(
            tournamentRepository = repository,
            mapper = mapper,
            geocoder = geocoder,
        )

        val emissions = mutableListOf<List<TournamentsTournamentWithDistance>>()
        val job = launch(Dispatchers.Unconfined) {
            useCase(locationFlow).collect { emissions.add(it) }
        }

        assertEquals(1, emissions.size)

        // Minor GPS noise (< 50m distance change) should be ignored
        locationFlow.value = jitterCoords
        assertEquals(1, emissions.size)

        // Significant location change (> 50m distance change) should trigger emission
        locationFlow.value = LocationCoordinates(41.3851, 2.1734)
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

    private class TrackingTournamentGeocoder(
        private val coordinatesMap: Map<String, LocationCoordinates>,
    ) : TournamentGeocoder {
        var callCount = 0
            private set

        override suspend fun geocodeBatch(locations: List<Pair<String?, String?>>): Map<String, LocationCoordinates?> {
            callCount++
            return locations.associate { (location, country) ->
                val key = buildLocationKey(location, country)
                key to coordinatesMap[key]
            }
        }

        override fun buildLocationKey(location: String?, country: String?): String {
            val locStr = location?.trim()?.lowercase().orEmpty()
            val countryStr = country?.trim()?.lowercase().orEmpty()
            if (locStr.isBlank() && countryStr.isBlank()) return ""
            return "$locStr|$countryStr"
        }
    }

    private class FakeExceptionHandler : ExceptionHandler {
        override fun handleException(throwable: Throwable) {
            // No-op for testing
        }
    }
}
