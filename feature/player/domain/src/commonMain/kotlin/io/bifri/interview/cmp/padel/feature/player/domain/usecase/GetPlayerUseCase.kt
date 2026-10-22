package io.bifri.interview.cmp.padel.feature.player.domain.usecase

import io.bifri.interview.cmp.padel.feature.player.data.repository.PlayerRepository
import io.bifri.interview.cmp.padel.feature.player.domain.mapper.toPlayer
import io.bifri.interview.cmp.padel.feature.player.domain.model.Player

class GetPlayerUseCase internal constructor(
    private val repository: PlayerRepository,
) {
    suspend operator fun invoke(id: Long): Player? = repository.getPlayer(id).toPlayer()
}
