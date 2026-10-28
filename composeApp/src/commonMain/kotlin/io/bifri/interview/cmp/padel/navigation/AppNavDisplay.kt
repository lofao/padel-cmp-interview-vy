package io.bifri.interview.cmp.padel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import io.bifri.interview.cmp.padel.core.ui.navigation.pop
import io.bifri.interview.cmp.padel.core.ui.navigation.rememberDefaultNavEntryDecorators
import io.bifri.interview.cmp.padel.feature.match.ui.navigation.MatchRoute.Matches
import io.bifri.interview.cmp.padel.feature.match.ui.navigation.matchEntries
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute.PlayerDetail
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute.Players
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.playerEntries
import io.bifri.interview.cmp.padel.feature.tournament.ui.navigation.tournamentEntries

@Composable
fun AppNavDisplay(
    backStack: NavBackStack<NavKey>,
    onError: (Throwable) -> Unit,
) {
    val onNavigate: (NavKey) -> Unit = { backStack.add(it) }
    val onBack: () -> Unit = { backStack.pop() }

    NavDisplay(
        backStack = backStack,
        entryDecorators = rememberDefaultNavEntryDecorators(),
        predictivePopTransitionSpec = { defaultPopTransitionSpec<NavKey>()() },
        entryProvider = entryProvider {
            tournamentEntries(
                onNavigate = onNavigate,
                onBack = onBack,
                onMatchesClick = { tournamentId, tournamentName ->
                    onNavigate(Matches(tournamentId, tournamentName))
                },
                onPlayersClick = { tournamentId, tournamentName ->
                    onNavigate(Players(tournamentId, tournamentName))
                },
                onError = onError,
            )
            matchEntries(
                onNavigate = onNavigate,
                onBack = onBack,
                onPlayerClick = { playerId -> onNavigate(PlayerDetail(playerId)) },
                onError = onError,
            )
            playerEntries(
                onNavigate = onNavigate,
                onBack = onBack,
                onError = onError,
            )
        },
    )
}
