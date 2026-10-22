package io.bifri.interview.cmp.padel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import io.bifri.interview.cmp.padel.core.ui.navigation.pop
import io.bifri.interview.cmp.padel.feature.match.ui.navigation.MatchNavDisplay
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerNavDisplay
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailRoute
import io.bifri.interview.cmp.padel.feature.tournament.ui.navigation.TournamentNavDisplay
import io.bifri.interview.cmp.padel.navigation.AppRoute.MatchGraph
import io.bifri.interview.cmp.padel.navigation.AppRoute.PlayerDetailGraph
import io.bifri.interview.cmp.padel.navigation.AppRoute.PlayerGraph
import io.bifri.interview.cmp.padel.navigation.AppRoute.TournamentGraph

@Composable
fun AppNavDisplay(
    backStack: NavBackStack<NavKey>,
    onError: (Throwable) -> Unit,
) {
    NavDisplay(
        backStack = backStack,
        predictivePopTransitionSpec = { defaultPopTransitionSpec<NavKey>()() },
        entryProvider = entryProvider {
            entry<TournamentGraph> {
                TournamentNavDisplay(
                    onMatchesClick = { tournamentId, tournamentName ->
                        backStack.add(MatchGraph(tournamentId, tournamentName))
                    },
                    onPlayersClick = { tournamentId, tournamentName ->
                        backStack.add(PlayerGraph(tournamentId, tournamentName))
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
                        backStack.add(PlayerDetailGraph(playerId))
                    },
                    onError = onError,
                )
            }
            entry<PlayerGraph> { route ->
                PlayerNavDisplay(
                    tournamentId = route.tournamentId,
                    tournamentName = route.tournamentName,
                    onBackClick = { backStack.pop() },
                    onError = onError,
                )
            }
            entry<PlayerDetailGraph> { route ->
                PlayerDetailRoute(
                    playerId = route.playerId,
                    onBackClick = { backStack.pop() },
                    onError = onError,
                )
            }
        },
    )
}
