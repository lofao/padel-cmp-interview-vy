package io.bifri.interview.cmp.padel.feature.player.domain.model

data class Player(
    val id: Long,
    val name: String,
    val ranking: Int?,
    val country: String?,
    val gender: String?,
    val photoUrl: String?,
    val position: String? = null,
    val matchesPlayed: Int? = null,
    val matchesWon: Int? = null,
    val points: Int? = null,
    val elo: Int? = null,
    val height: Int? = null,
    val hand: String? = null,
    val birthplace: String? = null,
    val birthdate: String? = null,
    val age: String? = null,
)
