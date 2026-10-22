package io.bifri.interview.cmp.padel.feature.match.ui.matches

import io.bifri.interview.cmp.padel.feature.match.domain.model.TournamentMatch

internal sealed interface MatchesPartialState {
    data object DataLoading : MatchesPartialState
    data class DataLoaded(val matches: List<TournamentMatch>) : MatchesPartialState
    data class DataLoadingError(val error: Throwable) : MatchesPartialState
    data class Navigate(val navigationAction: MatchesNavigationAction) : MatchesPartialState
}
