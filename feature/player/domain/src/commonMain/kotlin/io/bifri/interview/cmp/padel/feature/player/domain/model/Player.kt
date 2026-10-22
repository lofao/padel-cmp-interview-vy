package io.bifri.interview.cmp.padel.feature.player.domain.model

data class Player(
    val id: Long,
    val name: String,
    val ranking: Int?,
    val country: String?,
    val gender: String?,
    val photoUrl: String?,
)
