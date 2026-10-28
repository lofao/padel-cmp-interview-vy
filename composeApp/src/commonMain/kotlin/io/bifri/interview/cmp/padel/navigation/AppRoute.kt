package io.bifri.interview.cmp.padel.navigation

import androidx.navigation3.runtime.NavKey
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object TournamentGraph : AppRoute

    @Serializable
    data class PlayerGraph(
        val startRoute: PlayerRoute,
    ) : AppRoute

    @Serializable
    data class MatchGraph(
        val tournamentId: Long,
        val tournamentName: String,
    ) : AppRoute
}
