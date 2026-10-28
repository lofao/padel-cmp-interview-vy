package io.bifri.interview.cmp.padel.feature.tournament.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.bifri.interview.cmp.padel.feature.tournament.ui.navigation.TournamentRoute.Tournament
import io.bifri.interview.cmp.padel.feature.tournament.ui.navigation.TournamentRoute.Tournaments
import io.bifri.interview.cmp.padel.feature.tournament.ui.tournament.TournamentRoute
import io.bifri.interview.cmp.padel.feature.tournament.ui.tournaments.TournamentsRoute
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

fun PolymorphicModuleBuilder<NavKey>.tournamentRoutes() {
    subclass(Tournaments::class)
    subclass(Tournament::class)
}

fun EntryProviderScope<NavKey>.tournamentEntries(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    onMatchesClick: (tournamentId: Long, tournamentName: String) -> Unit,
    onPlayersClick: (tournamentId: Long, tournamentName: String) -> Unit,
    onError: (Throwable) -> Unit,
) {
    entry<Tournaments> {
        TournamentsRoute(
            onTournamentClick = { onNavigate(Tournament(tournamentId = it)) },
            onError = onError,
        )
    }
    entry<Tournament> { route ->
        TournamentRoute(
            tournamentId = route.tournamentId,
            onBackClick = onBack,
            onMatchesClick = { name -> onMatchesClick(route.tournamentId, name) },
            onPlayersClick = { name -> onPlayersClick(route.tournamentId, name) },
            onError = onError,
        )
    }
}
