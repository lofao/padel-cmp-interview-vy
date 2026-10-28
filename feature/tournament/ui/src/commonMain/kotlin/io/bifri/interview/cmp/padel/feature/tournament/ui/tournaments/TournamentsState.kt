package io.bifri.interview.cmp.padel.feature.tournament.ui.tournaments

import io.bifri.interview.cmp.padel.core.ui.Event
import io.bifri.interview.cmp.padel.feature.tournament.ui.model.TournamentUiModel

internal data class TournamentsState(
    val uiState: UiState = UiState(),
    val event: Event<TournamentsNavigationAction>? = null,
) {
    data class UiState(
        val items: List<TournamentUiModel> = emptyList(),
        val isDataLoading: Boolean = false,
    )
}
