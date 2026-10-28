package io.bifri.interview.cmp.padel.feature.tournament.ui.model

import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournament
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournamentWithDistance

data class TournamentUiModel(
    val tournament: TournamentsTournament,
    val distanceKm: Double? = null,
) {
    val id: Long get() = tournament.id
}

internal fun TournamentsTournamentWithDistance.toUiModel() = TournamentUiModel(
    tournament = tournament,
    distanceKm = distanceKm,
)
