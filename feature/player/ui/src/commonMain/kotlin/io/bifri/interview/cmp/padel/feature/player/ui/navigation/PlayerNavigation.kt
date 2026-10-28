package io.bifri.interview.cmp.padel.feature.player.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute.PlayerDetail
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute.Players
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailRoute
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersRoute
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

fun PolymorphicModuleBuilder<NavKey>.playerRoutes() {
    subclass(Players::class)
    subclass(PlayerDetail::class)
}

fun EntryProviderScope<NavKey>.playerEntries(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    onError: (Throwable) -> Unit,
) {
    entry<Players> { route ->
        PlayersRoute(
            tournamentName = route.tournamentName,
            onBackClick = onBack,
            onPlayerClick = { playerId -> onNavigate(PlayerDetail(playerId = playerId)) },
            onError = onError,
        )
    }
    entry<PlayerDetail> { route ->
        PlayerDetailRoute(
            playerId = route.playerId,
            onBackClick = onBack,
            onError = onError,
        )
    }
}
