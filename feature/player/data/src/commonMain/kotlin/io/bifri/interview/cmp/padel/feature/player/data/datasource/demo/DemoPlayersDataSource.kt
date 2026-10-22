package io.bifri.interview.cmp.padel.feature.player.data.datasource.demo

import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayersResponseDto
import io.bifri.interview.cmp.padel.feature.player.data.datasource.model.PlayersResponseDto.PlayerDto

internal class DemoPlayersDataSource {
    private val players = PlayersResponseDto(
        data = listOf(
            PlayerDto(
                id = 1L,
                name = "Arturo Coello",
                ranking = 1,
                country = "ES",
                gender = "male",
                photoUrl = null,
            ),
            PlayerDto(
                id = 2L,
                name = "Agustin Tapia",
                ranking = 2,
                country = "AR",
                gender = "male",
                photoUrl = null,
            ),
            PlayerDto(
                id = 3L,
                name = "Alejandro Galan",
                ranking = 3,
                country = "ES",
                gender = "male",
                photoUrl = null,
            ),
            PlayerDto(
                id = 4L,
                name = "Federico Chingotto",
                ranking = 4,
                country = "AR",
                gender = "male",
                photoUrl = null,
            ),
            PlayerDto(
                id = 5L,
                name = "Gemma Triay Pons",
                ranking = 1,
                country = "ES",
                gender = "female",
                photoUrl = null,
            ),
            PlayerDto(
                id = 6L,
                name = "Delfina Brea Senesi",
                ranking = 2,
                country = "AR",
                gender = "female",
                photoUrl = null,
            ),
            PlayerDto(
                id = 7L,
                name = "Claudia Fernandez Sanchez",
                ranking = 3,
                country = "ES",
                gender = "female",
                photoUrl = null,
            ),
            PlayerDto(
                id = 8L,
                name = "Martina Calvo Santamaria",
                ranking = 4,
                country = "ES",
                gender = "female",
                photoUrl = null,
            ),
            PlayerDto(
                id = 9L,
                name = "Martin Di Nenno",
                ranking = 5,
                country = "AR",
                gender = "male",
                photoUrl = null,
            ),
            PlayerDto(
                id = 10L,
                name = "Franco Stupaczuk",
                ranking = 6,
                country = "AR",
                gender = "male",
                photoUrl = null,
            ),
            PlayerDto(
                id = 11L,
                name = "Juan Lebron",
                ranking = 7,
                country = "ES",
                gender = "male",
                photoUrl = null,
            ),
            PlayerDto(
                id = 12L,
                name = "Francisco Navarro",
                ranking = 8,
                country = "ES",
                gender = "male",
                photoUrl = null,
            ),
        ),
    )

    fun getPlayers(): PlayersResponseDto = players
}
