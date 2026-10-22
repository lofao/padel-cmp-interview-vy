package io.bifri.interview.cmp.padel.feature.player.ui.navigation

import androidx.compose.runtime.Composable
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersRoute

@Composable
fun PlayerNavDisplay(
    tournamentId: Long,
    tournamentName: String,
    onBackClick: () -> Unit,
    onError: (Throwable) -> Unit,
) {
    PlayersRoute(
        tournamentId = tournamentId,
        tournamentName = tournamentName,
        onBackClick = onBackClick,
        onError = onError,
    )
}
