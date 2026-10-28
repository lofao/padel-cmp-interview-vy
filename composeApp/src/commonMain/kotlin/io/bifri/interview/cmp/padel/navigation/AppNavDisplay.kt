package io.bifri.interview.cmp.padel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import io.bifri.interview.cmp.padel.core.ui.navigation.pop
import io.bifri.interview.cmp.padel.core.ui.navigation.rememberDefaultNavEntryDecorators
import io.bifri.interview.cmp.padel.feature.match.ui.navigation.MatchNavDisplay
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerNavDisplay
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute.PlayerDetail
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute.Players
import io.bifri.interview.cmp.padel.feature.tournament.ui.navigation.TournamentNavDisplay
import io.bifri.interview.cmp.padel.navigation.AppRoute.MatchGraph
import io.bifri.interview.cmp.padel.navigation.AppRoute.PlayerGraph
import io.bifri.interview.cmp.padel.navigation.AppRoute.TournamentGraph

@Composable
fun AppNavDisplay(
    backStack: NavBackStack<NavKey>,
    onError: (Throwable) -> Unit,
) {
    NavDisplay(
        backStack = backStack,
        entryDecorators = rememberDefaultNavEntryDecorators(),
        predictivePopTransitionSpec = { defaultPopTransitionSpec<NavKey>()() },
        entryProvider = entryProvider {
            entry<TournamentGraph> {
                TournamentNavDisplay(
                    onMatchesClick = { tournamentId, tournamentName ->
                        backStack.add(MatchGraph(tournamentId, tournamentName))
                    },
                    onPlayersClick = { tournamentId, tournamentName ->
                        backStack.add(PlayerGraph(Players(tournamentId, tournamentName)))
                    },
                    onError = onError,
                )
            }
            entry<MatchGraph> { route ->
                MatchNavDisplay(
                    tournamentId = route.tournamentId,
                    tournamentName = route.tournamentName,
                    onBackClick = { backStack.pop() },
                    onPlayerClick = { playerId ->
                        backStack.add(PlayerGraph(PlayerDetail(playerId)))
                    },
                    onError = onError,
                )
            }
            entry<PlayerGraph> { route ->
                PlayerNavDisplay(
                    startRoute = route.startRoute,
                    onBackClick = { backStack.pop() },
                    onError = onError,
                )
            }
        },
    )
}
