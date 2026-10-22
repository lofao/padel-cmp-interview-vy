package io.bifri.interview.cmp.padel.feature.player.data.datasource.remote

import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayerDetailDto
import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayersResponseDto

internal interface PlayerRemoteDataSource {
    suspend fun getPlayers(): PlayersResponseDto
    suspend fun getPlayer(id: Long): PlayerDetailDto
}
