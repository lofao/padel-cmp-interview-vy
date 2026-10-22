package io.bifri.interview.cmp.padel.feature.player.ui.player

import androidx.lifecycle.viewModelScope
import io.bifri.interview.cmp.padel.core.coroutines.extension.mutableSharedFlowOfInput
import io.bifri.interview.cmp.padel.core.coroutines.extension.stateInViewModel
import io.bifri.interview.cmp.padel.core.exception.ExceptionHandler
import io.bifri.interview.cmp.padel.core.ui.Event
import io.bifri.interview.cmp.padel.core.ui.base.BaseViewModel
import io.bifri.interview.cmp.padel.feature.player.domain.usecase.GetPlayerUseCase
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailNavigationAction.ShowError
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailPartialState.Loading
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailPartialState.LoadingFailed
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailPartialState.Navigate
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailPartialState.PlayerLoaded
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold

class PlayerDetailViewModel(
    private val playerId: Long,
    coroutineDispatcher: CoroutineDispatcher,
    exceptionHandler: ExceptionHandler,
    private val getPlayerUseCase: GetPlayerUseCase,
) : BaseViewModel(coroutineDispatcher, exceptionHandler) {

    private val refreshClicks = mutableSharedFlowOfInput<Unit>()
    private val state: Flow<PlayerDetailState> = _state
    internal val uiState = state.map { it.uiState }
    internal val events = state.mapNotNull { it.event }

    private val _state: Flow<PlayerDetailState>
        get() = refreshClicksStream()
            .catch {
                exceptionHandler.handleException(it)
                emit(Navigate(ShowError(it)))
            }
            .runningFold(PlayerDetailState(), ::viewStateReducer)
            .flowOn(coroutineDispatcher)
            .stateInViewModel(
                scope = viewModelScope,
                initialValue = PlayerDetailState(),
            )

    internal fun onRefresh() {
        refreshClicks.tryEmit(Unit)
    }

    private fun playerStream(): Flow<PlayerDetailPartialState> = flow {
        emit(Loading)
        emit(PlayerLoaded(getPlayerUseCase(playerId)))
    }
        .catch { emit(LoadingFailed(it)) }

    private fun refreshClicksStream(): Flow<PlayerDetailPartialState> = refreshClicks
        .onStart { emit(Unit) }
        .flatMapLatest { playerStream() }

    private fun viewStateReducer(
        prevState: PlayerDetailState,
        changes: PlayerDetailPartialState,
    ): PlayerDetailState = when (changes) {
        Loading -> prevState.copy(
            uiState = prevState.uiState.copy(isDataLoading = true),
            event = null,
        )

        is PlayerLoaded -> prevState.copy(
            uiState = prevState.uiState.copy(
                player = changes.player,
                isDataLoading = false,
            ),
            event = null,
        )

        is LoadingFailed -> prevState.copy(
            uiState = prevState.uiState.copy(isDataLoading = false),
            event = Event(ShowError(changes.error)),
        )

        is Navigate -> prevState.copy(event = Event(changes.navigationAction))
    }
}
