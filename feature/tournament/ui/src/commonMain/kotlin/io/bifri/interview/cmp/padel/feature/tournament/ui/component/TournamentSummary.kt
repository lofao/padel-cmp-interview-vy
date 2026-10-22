package io.bifri.interview.cmp.padel.feature.tournament.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.bifri.interview.cmp.padel.core.designsystem.theme.dimens
import io.bifri.interview.cmp.padel.feature.tournament.domain.model.TournamentsTournament

@Composable
internal fun TournamentSummary(
    tournament: TournamentsTournament,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
    ) {
        val tournamentName = tournament.name
        if (tournamentName != null) {
            Text(
                text = tournamentName,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Text(
            text = tournament.formatLevelAndStatus(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(text = tournament.formatLocationAndDistance())
        Text(
            text = tournament.formatDateRange(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun TournamentsTournament.formatLevelAndStatus() = listOfNotNull(level, status)
    .filter { it.isNotBlank() }
    .joinToString(" • ") { it.replace('_', ' ').uppercase() }

private fun TournamentsTournament.formatLocationAndDistance(): String {
    val locationPart = listOfNotNull(location, country).filter { it.isNotBlank() }.joinToString(", ")
    val distancePart = distanceKm?.let { formatDistance(it) }
    return listOfNotNull(locationPart.ifBlank { null }, distancePart).joinToString(" • ")
}

@Suppress("MagicNumber")
private fun formatDistance(distanceKm: Double): String = if (distanceKm < 1.0) {
    "${(distanceKm * 1000).toInt()} m away"
} else {
    "${distanceKm.toInt()} km away"
}

private fun TournamentsTournament.formatDateRange() = listOfNotNull(startDate, endDate).joinToString(" – ")
