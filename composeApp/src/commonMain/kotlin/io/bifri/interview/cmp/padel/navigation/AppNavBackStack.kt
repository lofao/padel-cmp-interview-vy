package io.bifri.interview.cmp.padel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import io.bifri.interview.cmp.padel.feature.match.ui.navigation.matchRoutes
import io.bifri.interview.cmp.padel.feature.player.ui.navigation.playerRoutes
import io.bifri.interview.cmp.padel.feature.tournament.ui.navigation.TournamentRoute.Tournaments
import io.bifri.interview.cmp.padel.feature.tournament.ui.navigation.tournamentRoutes
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val AppNavConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = NavKey::class) {
            tournamentRoutes()
            matchRoutes()
            playerRoutes()
        }
    }
}

@Composable
internal fun rememberAppNavBackStack(): NavBackStack<NavKey> =
    rememberNavBackStack(AppNavConfiguration, Tournaments)
