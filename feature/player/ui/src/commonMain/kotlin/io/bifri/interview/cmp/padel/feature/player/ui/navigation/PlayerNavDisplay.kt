package io.bifri.interview.cmp.padel.feature.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.savedstate.serialization.SavedStateConfiguration
import io.bifri.interview.cmp.padel.core.ui.navigation.rememberDefaultNavEntryDecorators
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute.PlayerDetail
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.PlayerRoute.Players
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailRoute
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersRoute
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val PlayerNavConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = NavKey::class) {
            subclass(Players::class)
            subclass(PlayerDetail::class)
        }
    }
}

@Composable
fun PlayerNavDisplay(
    startRoute: PlayerRoute,
    onBackClick: () -> Unit,
    onError: (Throwable) -> Unit,
) {
    val backStack = rememberNavBackStack(PlayerNavConfiguration, startRoute)
    val navigateBack: () -> Unit = {
        if (backStack.size > 1) backStack.removeLastOrNull() else onBackClick()
    }

    NavDisplay(
        backStack = backStack,
        entryDecorators = rememberDefaultNavEntryDecorators(),
        predictivePopTransitionSpec = { defaultPopTransitionSpec<NavKey>()() },
        entryProvider = entryProvider {
            entry<Players> { route ->
                PlayersRoute(
                    tournamentName = route.tournamentName,
                    onBackClick = navigateBack,
                    onPlayerClick = { playerId ->
                        backStack.add(PlayerDetail(playerId = playerId))
                    },
                    onError = onError,
                )
            }
            entry<PlayerDetail> { route ->
                PlayerDetailRoute(
                    playerId = route.playerId,
                    onBackClick = navigateBack,
                    onError = onError,
                )
            }
        },
    )
}
