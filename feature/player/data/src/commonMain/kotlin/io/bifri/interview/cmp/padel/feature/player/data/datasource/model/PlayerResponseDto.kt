package io.bifri.interview.cmp.padel.feature.player.data.datasource.model

import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayersResponseDto.PlayerDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerResponseDto(
    @SerialName("data") val data: PlayerDto? = null,
)
