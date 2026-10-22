package io.bifri.interview.cmp.padel.feature.player.ui.player

sealed interface PlayerDetailNavigationAction {
    data class ShowError(val error: Throwable) : PlayerDetailNavigationAction
}
