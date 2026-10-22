package io.bifri.interview.cmp.padel.feature.player.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface PlayerRoute : NavKey {
    @Serializable
    data class Players(
        val tournamentId: Long,
        val tournamentName: String,
    ) : PlayerRoute

    @Serializable
    data class PlayerDetail(
        val playerId: Long,
    ) : PlayerRoute
}
