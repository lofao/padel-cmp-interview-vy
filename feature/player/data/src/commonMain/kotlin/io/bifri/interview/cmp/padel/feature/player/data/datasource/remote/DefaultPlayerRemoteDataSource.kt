package io.bifri.interview.cmp.padel.feature.player.data.datasource.remote

import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayersResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class DefaultPlayerRemoteDataSource(
    private val httpClient: HttpClient,
) : PlayerRemoteDataSource {
    override suspend fun getPlayers(): PlayersResponseDto =
        httpClient.get("/api/players").body()
}
