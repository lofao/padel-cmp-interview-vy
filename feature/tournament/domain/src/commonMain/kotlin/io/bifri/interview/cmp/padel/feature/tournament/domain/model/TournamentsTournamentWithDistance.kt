package io.bifri.interview.cmp.padel.feature.tournament.domain.model

data class TournamentsTournamentWithDistance(
    val tournament: TournamentsTournament,
    val distanceKm: Double? = null,
)
