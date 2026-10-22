package io.bifri.interview.cmp.padel.feature.player.data.repository

import io.bifri.interview.cmp.padel.feature.player.data.datasource.demo.DemoPlayersDataSource

internal class DemoPlayerRepository(
    private val demoPlayersDataSource: DemoPlayersDataSource,
) : PlayerRepository {
    override suspend fun getPlayers() = demoPlayersDataSource.getPlayers()
    override suspend fun getPlayer(id: Long) = demoPlayersDataSource.getPlayer(id)
}
