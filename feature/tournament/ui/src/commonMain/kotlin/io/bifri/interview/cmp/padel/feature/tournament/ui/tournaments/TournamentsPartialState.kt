package io.bifri.interview.cmp.padel.feature.tournament.ui.tournaments

import io.bifri.interview.cmp.padel.feature.tournament.ui.model.TournamentUiModel

internal sealed interface TournamentsPartialState {
    data object DataLoading : TournamentsPartialState
    data class DataLoaded(val items: List<TournamentUiModel>) : TournamentsPartialState
    data class DataLoadingError(val error: Throwable) : TournamentsPartialState
    data class Navigate(val navigationAction: TournamentsNavigationAction) : TournamentsPartialState
}
