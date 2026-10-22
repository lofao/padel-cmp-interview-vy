package io.bifri.interview.cmp.padel.feature.player.ui.players

import io.bifri.interview.cmp.padel.feature.player.domain.model.Player
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayersViewModelTest {

    @Test
    fun playerState_filteringByName_returnsMatchingItems() {
        val player1 = Player(1L, "Arturo Coello", 1, "ES", "male", null)
        val player2 = Player(2L, "Agustin Tapia", 2, "AR", "male", null)
        val state = PlayersState.UiState(
            items = listOf(player1, player2),
            searchQuery = "coello",
        )

        val filtered = state.filteredItems
        assertEquals(1, filtered.size)
        assertEquals("Arturo Coello", filtered.first().name)
    }

    @Test
    fun playerState_emptyQuery_returnsAllItems() {
        val player1 = Player(1L, "Arturo Coello", 1, "ES", "male", null)
        val player2 = Player(2L, "Agustin Tapia", 2, "AR", "male", null)
        val state = PlayersState.UiState(
            items = listOf(player1, player2),
            searchQuery = "",
        )

        assertEquals(2, state.filteredItems.size)
    }
}
