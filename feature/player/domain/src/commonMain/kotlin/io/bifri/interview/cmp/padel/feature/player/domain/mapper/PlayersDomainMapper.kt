package io.bifri.interview.cmp.padel.feature.player.domain.mapper

import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayerResponseDto
import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayersResponseDto
import io.bifri.interview.cmp.padel.feature.player.domain.model.Player

internal fun PlayersResponseDto.toPlayers(): List<Player> =
    data?.mapNotNull { it?.toPlayer() }.orEmpty()

internal fun PlayerResponseDto?.toPlayer(): Player? {
    this ?: return null
    val playerId = id ?: return null
    val playerName = name ?: return null

    val genderValue = when (category?.lowercase()) {
        "men" -> "male"
        "women" -> "female"
        else -> null
    }

    return Player(
        id = playerId,
        name = playerName,
        ranking = ranking?.toIntOrNull(),
        country = nationality ?: country,
        gender = genderValue,
        photoUrl = photoUrl,
        position = side,
        points = points,
        elo = elo,
        height = height,
        hand = hand,
        birthplace = birthplace,
        birthdate = birthdate,
        age = age,
    )
}

internal fun PlayersResponseDto.PlayerDto.toPlayer(): Player? {
    val playerId = id ?: return null
    val playerName = name ?: return null

    return Player(
        id = playerId,
        name = playerName,
        ranking = ranking,
        country = country,
        gender = gender,
        photoUrl = photoUrl,
        position = position,
        matchesPlayed = matchesPlayed,
        matchesWon = matchesWon,
        points = points,
    )
}
