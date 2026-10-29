package io.bifri.interview.cmp.padel.feature.tournament.ui.di

import io.bifri.interview.cmp.padel.core.coroutines.di.CoroutinesDiQualifier.IoDispatcher
import io.bifri.interview.cmp.padel.feature.tournament.domain.repository.TournamentGeocoder
import io.bifri.interview.cmp.padel.feature.tournament.ui.geocoder.DefaultTournamentGeocoder
import io.bifri.interview.cmp.padel.feature.tournament.ui.tournament.TournamentViewModel
import io.bifri.interview.cmp.padel.feature.tournament.ui.tournaments.TournamentsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val TournamentUiModule = module {
    single<TournamentGeocoder> { DefaultTournamentGeocoder(cachedGeocoderService = get()) }
    viewModel {
        TournamentsViewModel(
            coroutineDispatcher = get(IoDispatcher.koinQualifier),
            exceptionHandler = get(),
            getTournamentsUseCase = get(),
        )
    }
    viewModel { (tournamentId: Long) ->
        TournamentViewModel(
            tournamentId = tournamentId,
            coroutineDispatcher = get(IoDispatcher.koinQualifier),
            exceptionHandler = get(),
            getTournamentUseCase = get(),
        )
    }
}
