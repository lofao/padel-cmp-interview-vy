package io.bifri.interview.cmp.padel.feature.player.ui.players

import androidx.lifecycle.viewModelScope
import io.bifri.interview.cmp.padel.core.coroutines.extension.mutableSharedFlowOfInput
import io.bifri.interview.cmp.padel.core.coroutines.extension.stateInViewModel
import io.bifri.interview.cmp.padel.core.exception.ExceptionHandler
import io.bifri.interview.cmp.padel.core.ui.Event
import io.bifri.interview.cmp.padel.core.ui.base.BaseViewModel
import io.bifri.interview.cmp.padel.feature.player.domain.usecase.GetPlayersUseCase
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersNavigationAction.ShowError
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.Loading
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.LoadingFailed
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.Navigate
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.PlayersLoaded
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.SearchQueryChanged
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold

class PlayersViewModel(
    private val tournamentName: String,
    coroutineDispatcher: CoroutineDispatcher,
    exceptionHandler: ExceptionHandler,
    private val getPlayersUseCase: GetPlayersUseCase,
) : BaseViewModel(coroutineDispatcher, exceptionHandler) {

    private val refreshClicks = mutableSharedFlowOfInput<Unit>()
    private val searchQueryInput = mutableSharedFlowOfInput<String>()
    private val state: Flow<PlayersState> = _state
    internal val uiState = state.map { it.uiState }
    internal val events = state.mapNotNull { it.event }

    private val _state: Flow<PlayersState>
        get() = merge(
            refreshClicksStream(),
            searchQueryStream(),
        )
            .catch {
                exceptionHandler.handleException(it)
                emit(Navigate(ShowError(it)))
            }
            .runningFold(PlayersState(), ::viewStateReducer)
            .flowOn(coroutineDispatcher)
            .stateInViewModel(
                scope = viewModelScope,
                initialValue = PlayersState(),
            )

    internal fun onRefresh() {
        refreshClicks.tryEmit(Unit)
    }

    internal fun onSearchQueryChange(query: String) {
        searchQueryInput.tryEmit(query)
    }

    internal fun onClearSearchQuery() {
        searchQueryInput.tryEmit("")
    }

    private fun playersStream(): Flow<PlayersPartialState> = flow {
        emit(Loading)
        emit(PlayersLoaded(getPlayersUseCase()))
    }
        .catch { emit(LoadingFailed(it)) }

    private fun refreshClicksStream(): Flow<PlayersPartialState> = refreshClicks
        .onStart { emit(Unit) }
        .flatMapLatest { playersStream() }

    private fun searchQueryStream(): Flow<PlayersPartialState> = searchQueryInput
        .map { SearchQueryChanged(it) }

    private fun viewStateReducer(
        prevState: PlayersState,
        changes: PlayersPartialState,
    ): PlayersState = when (changes) {
        Loading -> prevState.copy(
            uiState = prevState.uiState.copy(isDataLoading = true),
            event = null,
        )

        is PlayersLoaded -> prevState.copy(
            uiState = prevState.uiState.copy(
                items = changes.players,
                isDataLoading = false,
                tournamentName = tournamentName,
            ),
            event = null,
        )

        is LoadingFailed -> prevState.copy(
            uiState = prevState.uiState.copy(isDataLoading = false),
            event = Event(ShowError(changes.error)),
        )

        is SearchQueryChanged -> prevState.copy(
            uiState = prevState.uiState.copy(searchQuery = changes.query),
            event = null,
        )

        is Navigate -> prevState.copy(event = Event(changes.navigationAction))
    }
}
