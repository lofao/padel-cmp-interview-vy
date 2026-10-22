package io.bifri.interview.cmp.padel.feature.player.data.datasource.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayersResponseDto(
    @SerialName("data") val data: List<PlayerDto?>? = null,
) {
    @Serializable
    data class PlayerDto(
        @SerialName("id") val id: Long? = null,
        @SerialName("name") val name: String? = null,
        @SerialName("ranking") val ranking: Int? = null,
        @SerialName("country") val country: String? = null,
        @SerialName("gender") val gender: String? = null,
        @SerialName("photo_url") val photoUrl: String? = null,
    )
}
