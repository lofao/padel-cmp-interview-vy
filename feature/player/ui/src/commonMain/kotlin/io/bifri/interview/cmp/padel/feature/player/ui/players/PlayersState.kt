package io.bifri.interview.cmp.padel.feature.player.ui.players

import io.bifri.interview.cmp.padel.core.ui.Event
import io.bifri.interview.cmp.padel.feature.player.domain.model.Player

internal data class PlayersState(
    val uiState: UiState = UiState(),
    val event: Event<PlayersNavigationAction>? = null,
) {
    data class UiState(
        val items: List<Player> = emptyList(),
        val searchQuery: String = "",
        val isDataLoading: Boolean = false,
        val tournamentName: String? = null,
    ) {
        val filteredItems: List<Player>
            get() = if (searchQuery.isBlank()) {
                items
            } else {
                items.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
    }
}
