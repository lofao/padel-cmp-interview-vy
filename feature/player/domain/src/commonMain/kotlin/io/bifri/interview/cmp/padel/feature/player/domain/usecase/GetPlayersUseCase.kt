package io.bifri.interview.cmp.padel.feature.player.domain.usecase

import io.bifri.interview.cmp.padel.feature.player.data.repository.PlayerRepository
import io.bifri.interview.cmp.padel.feature.player.domain.mapper.toPlayers
import io.bifri.interview.cmp.padel.feature.player.domain.model.Player

class GetPlayersUseCase internal constructor(
    private val repository: PlayerRepository,
) {
    suspend operator fun invoke(): List<Player> = repository.getPlayers().toPlayers()
}
