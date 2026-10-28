package io.bifri.interview.cmp.padel.feature.player.ui.players

import androidx.lifecycle.viewModelScope
import io.bifri.interview.cmp.padel.core.coroutines.extension.mutableSharedFlowOfInput
import io.bifri.interview.cmp.padel.core.coroutines.extension.stateInViewModel
import io.bifri.interview.cmp.padel.core.exception.ExceptionHandler
import io.bifri.interview.cmp.padel.core.ui.Event
import io.bifri.interview.cmp.padel.core.ui.base.BaseViewModel
import io.bifri.interview.cmp.padel.feature.player.domain.usecase.GetPlayersUseCase
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersNavigationAction.ShowError
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.DataLoaded
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.DataLoading
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.DataLoadingError
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersPartialState.Navigate
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

    private val refreshes = mutableSharedFlowOfInput<Unit>()
    private val searchQueryInput = mutableSharedFlowOfInput<String>()
    private val state: Flow<PlayersState> = _state
    internal val uiState = state.map { it.uiState }
    internal val events = state.mapNotNull { it.event }

    private val _state: Flow<PlayersState>
        get() = merge(
            dataStream(),
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
        refreshes.tryEmit(Unit)
    }

    internal fun onSearchQueryChange(query: String) {
        searchQueryInput.tryEmit(query)
    }

    internal fun onClearSearchQuery() {
        searchQueryInput.tryEmit("")
    }

    private fun dataStream(): Flow<PlayersPartialState> = refreshes
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow { emit(getPlayersUseCase()) }
                .map(::DataLoaded)
                .onStart<PlayersPartialState> { emit(DataLoading) }
                .catch {
                    exceptionHandler.handleException(it)
                    emit(DataLoadingError(it))
                }
        }

    private fun searchQueryStream(): Flow<PlayersPartialState> = searchQueryInput
        .map { SearchQueryChanged(it) }

    private fun viewStateReducer(
        prevState: PlayersState,
        changes: PlayersPartialState,
    ): PlayersState = when (changes) {
        DataLoading -> prevState.copy(
            uiState = prevState.uiState.copy(isDataLoading = true),
            event = null,
        )

        is DataLoaded -> prevState.copy(
            uiState = prevState.uiState.copy(
                items = changes.players,
                isDataLoading = false,
                tournamentName = tournamentName,
            ),
            event = null,
        )

        is DataLoadingError -> prevState.copy(
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
