@file:Suppress("TooManyFunctions")

package io.bifri.interview.cmp.padel.feature.player.ui.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import io.bifri.interview.cmp.padel.core.designsystem.component.appbar.CollapsingTopAppBar
import io.bifri.interview.cmp.padel.core.designsystem.component.avatar.PlayerAvatar
import io.bifri.interview.cmp.padel.core.designsystem.component.button.BackIconButton
import io.bifri.interview.cmp.padel.core.designsystem.component.placeholder.ShimmerBox
import io.bifri.interview.cmp.padel.core.designsystem.component.pulltorefresh.AppPullToRefreshBox
import io.bifri.interview.cmp.padel.core.designsystem.component.scaffold.AppScaffold
import io.bifri.interview.cmp.padel.core.designsystem.theme.AppTheme
import io.bifri.interview.cmp.padel.core.designsystem.theme.dimens
import io.bifri.interview.cmp.padel.core.ui.Event
import io.bifri.interview.cmp.padel.core.ui.LifecycleCollectEffect
import io.bifri.interview.cmp.padel.feature.player.domain.model.Player
import io.bifri.interview.cmp.padel.feature.player.ui.PlayerUiRes
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersNavigationAction.ShowError
import io.bifri.interview.cmp.padel.feature.player.ui.players.PlayersState.UiState
import io.bifri.interview.cmp.padel.feature.player.ui.players_avatar_cd
import io.bifri.interview.cmp.padel.feature.player.ui.players_clear_search_cd
import io.bifri.interview.cmp.padel.feature.player.ui.players_country
import io.bifri.interview.cmp.padel.feature.player.ui.players_empty
import io.bifri.interview.cmp.padel.feature.player.ui.players_gender
import io.bifri.interview.cmp.padel.feature.player.ui.players_rank
import io.bifri.interview.cmp.padel.feature.player.ui.players_screen_title
import io.bifri.interview.cmp.padel.feature.player.ui.players_search_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val LoadingSkeletonCount = 7
private val LoadingSkeletonHeight = 80.dp
private val PlayerAvatarSize = 48.dp

@Composable
fun PlayersRoute(
    tournamentName: String,
    onBackClick: () -> Unit,
    onError: (Throwable) -> Unit,
    onPlayerClick: (Long) -> Unit = {},
    viewModel: PlayersViewModel = koinViewModel {
        parametersOf(tournamentName)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(null)

    PlayersScreen(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearchQuery = viewModel::onClearSearchQuery,
        onBackClick = onBackClick,
        onPlayerClick = onPlayerClick,
    )

    LifecycleCollectEffect(
        stream = viewModel.events,
    ) {
        processEvents(
            event = it,
            onError = onError,
        )
    }
}

@Composable
private fun PlayersScreen(
    uiState: UiState?,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPlayerClick: (Long) -> Unit = {},
) {
    if (uiState == null) return

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
    )

    AppScaffold(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        topBar = {
            CollapsingTopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimens.spacingExtraSmall),
                    ) {
                        Text(text = stringResource(PlayerUiRes.string.players_screen_title))
                        Text(
                            text = uiState.tournamentName.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = { BackIconButton(onClick = onBackClick) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val statusBarTopPadding = TopAppBarDefaults.windowInsets.asPaddingValues().calculateTopPadding()
        val columnTopPadding = maxOf(innerPadding.calculateTopPadding(), statusBarTopPadding)

        AppPullToRefreshBox(
            isRefreshing = uiState.isDataLoading,
            onRefresh = onRefresh,
            topPadding = columnTopPadding,
        ) {
            val listContentPadding = PaddingValues(
                start = dimens.margin,
                end = dimens.margin,
                top = dimens.spacingSmall,
                bottom = innerPadding.calculateBottomPadding() + dimens.margin,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = columnTopPadding),
            ) {
                SearchInputField(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClearClick = onClearSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.margin, vertical = dimens.spacingSmall),
                )

                when {
                    uiState.isDataLoading && uiState.items.isEmpty() -> LoadingPlayersContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = listContentPadding,
                    )

                    uiState.filteredItems.isEmpty() -> EmptyPlayersContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(listContentPadding),
                    )

                    else -> PlayersContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        items = uiState.filteredItems,
                        contentPadding = listContentPadding,
                        onPlayerClick = onPlayerClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = stringResource(PlayerUiRes.string.players_search_hint),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(PlayerUiRes.string.players_clear_search_cd),
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun LoadingPlayersContent(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
    ) {
        items(LoadingSkeletonCount) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LoadingSkeletonHeight)
                    .clip(MaterialTheme.shapes.small),
            )
        }
    }
}

@Composable
private fun EmptyPlayersContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(PlayerUiRes.string.players_empty))
    }
}

@Composable
private fun PlayersContent(
    items: List<Player>,
    contentPadding: PaddingValues,
    onPlayerClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = items,
            key = Player::id,
        ) { player ->
            PlayerListItem(
                player = player,
                onClick = { onPlayerClick(player.id) },
            )
        }
    }
}

@Composable
private fun PlayerListItem(
    player: Player,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.cardPaddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
        ) {
            PlayerAvatar(
                photoUrl = player.photoUrl,
                contentDescription = stringResource(PlayerUiRes.string.players_avatar_cd, player.name),
                modifier = Modifier.size(PlayerAvatarSize),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingExtraSmall),
            ) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                player.gender?.takeIf { it.isNotBlank() }?.let { gender ->
                    Text(
                        text = stringResource(
                            PlayerUiRes.string.players_gender,
                            gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                player.country?.takeIf { it.isNotBlank() }?.let { country ->
                    Text(
                        text = stringResource(PlayerUiRes.string.players_country, country),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                player.ranking?.let { rank ->
                    Text(
                        text = stringResource(PlayerUiRes.string.players_rank, rank),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

private fun processEvents(
    event: Event<PlayersNavigationAction>,
    onError: (Throwable) -> Unit,
) {
    val action = event.getContentIfNotHandled() ?: return
    when (action) {
        is ShowError -> onError(action.error)
    }
}

@Preview(name = "Players")
@Composable
private fun PlayersScreenPreview() {
    AppTheme {
        PlayersScreen(
            uiState = UiState(
                items = listOf(
                    Player(
                        id = 1L,
                        name = "Arturo Coello",
                        ranking = 1,
                        country = "ES",
                        gender = "male",
                        photoUrl = null,
                    ),
                    Player(
                        id = 2L,
                        name = "Agustin Tapia",
                        ranking = 2,
                        country = "AR",
                        gender = "male",
                        photoUrl = null,
                    ),
                ),
                isDataLoading = false,
                tournamentName = "Madrid P1",
            ),
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onRefresh = {},
        )
    }
}
