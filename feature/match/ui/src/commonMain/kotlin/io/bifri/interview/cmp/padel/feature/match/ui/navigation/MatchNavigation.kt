package io.bifri.interview.cmp.padel.feature.match.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.bifri.interview.cmp.padel.feature.match.ui.match.MatchRoute
import io.bifri.interview.cmp.padel.feature.match.ui.matches.MatchesRoute
import io.bifri.interview.cmp.padel.feature.match.ui.navigation.MatchRoute.Match
import io.bifri.interview.cmp.padel.feature.match.ui.navigation.MatchRoute.Matches
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

fun PolymorphicModuleBuilder<NavKey>.matchRoutes() {
    subclass(Matches::class)
    subclass(Match::class)
}

fun EntryProviderScope<NavKey>.matchEntries(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    onPlayerClick: (playerId: Long) -> Unit,
    onError: (Throwable) -> Unit,
) {
    entry<Matches> { route ->
        MatchesRoute(
            tournamentId = route.tournamentId,
            tournamentName = route.tournamentName,
            onBackClick = onBack,
            onMatchClick = { matchId, totalCourts ->
                onNavigate(
                    Match(
                        matchId = matchId,
                        tournamentName = route.tournamentName,
                        totalCourts = totalCourts,
                    ),
                )
            },
            onError = onError,
        )
    }
    entry<Match> { route ->
        MatchRoute(
            matchId = route.matchId,
            tournamentName = route.tournamentName,
            totalCourts = route.totalCourts,
            onBackClick = onBack,
            onPlayerClick = onPlayerClick,
            onError = onError,
        )
    }
}
