package io.bifri.interview.cmp.padel.feature.player.ui.player

import io.bifri.interview.cmp.padel.feature.player.domain.model.Player

internal sealed interface PlayerDetailPartialState {
    data object Loading : PlayerDetailPartialState
    data class PlayerLoaded(val player: Player?) : PlayerDetailPartialState
    data class LoadingFailed(val error: Throwable) : PlayerDetailPartialState
    data class Navigate(val navigationAction: PlayerDetailNavigationAction) : PlayerDetailPartialState
}
