package io.bifri.interview.cmp.padel.feature.player.domain.mapper

import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayersResponseDto
import io.bifri.interview.cmp.padel.feature.player.domain.model.Player

internal fun PlayersResponseDto.toPlayers(): List<Player> =
    data?.mapNotNull { it?.toPlayer() }.orEmpty()

private fun PlayersResponseDto.PlayerDto.toPlayer(): Player? {
    val playerId = id ?: return null
    val playerName = name ?: return null

    return Player(
        id = playerId,
        name = playerName,
        ranking = ranking,
        country = country,
        gender = gender,
        photoUrl = photoUrl,
    )
}
