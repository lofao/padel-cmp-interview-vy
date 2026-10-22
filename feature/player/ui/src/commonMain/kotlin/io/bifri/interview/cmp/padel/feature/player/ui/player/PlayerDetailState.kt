package io.bifri.interview.cmp.padel.feature.player.ui.player

import io.bifri.interview.cmp.padel.core.ui.Event
import io.bifri.interview.cmp.padel.feature.player.domain.model.Player

internal data class PlayerDetailState(
    val uiState: UiState = UiState(),
    val event: Event<PlayerDetailNavigationAction>? = null,
) {
    data class UiState(
        val player: Player? = null,
        val isDataLoading: Boolean = false,
    )
}
