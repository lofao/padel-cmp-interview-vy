package io.bifri.interview.cmp.padel.feature.player.ui.player

import io.bifri.interview.cmp.padel.feature.player.domain.model.Player
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailState.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayerDetailViewModelTest {

    @Test
    fun playerDetailState_initialState_hasNullPlayer() {
        val state = UiState()
        assertNull(state.player)
        assertEquals(false, state.isDataLoading)
    }

    @Test
    fun playerDetailState_loadedPlayer_returnsCorrectData() {
        val player = Player(
            id = 1L,
            name = "Arturo Coello",
            ranking = 1,
            country = "ES",
            gender = "male",
            photoUrl = null,
            position = "Drive",
            matchesPlayed = 98,
            matchesWon = 88,
            points = 14200,
        )
        val state = UiState(
            player = player,
            isDataLoading = false,
        )

        assertEquals("Arturo Coello", state.player?.name)
        assertEquals(1, state.player?.ranking)
        assertEquals("Drive", state.player?.position)
    }
}
